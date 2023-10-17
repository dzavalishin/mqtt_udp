namespace MqttUdp
{
    using System;
    using System.Collections.Generic;
    using System.Net;
    using System.Net.Sockets;
    using System.Linq;
    using System.Threading.Tasks;
    using System.Collections.Concurrent;
    using System.Threading;
    using System.Globalization;
    using Microsoft.Extensions.Logging;

    public class Server
    {
        public const int DefaultPort = 1883;
        public static readonly IPEndPoint DefaultAddress = new IPEndPoint(IPAddress.Broadcast, DefaultPort);
        public int MaxSize = 512;
        public readonly PublishOptions DefaultPublishOptions = new PublishOptions();
        readonly List<TopicFilter> topics = new List<TopicFilter>();
        readonly Dictionary<string, byte[]> retainedMessages = new Dictionary<string, byte[]>();
        readonly RateGate? RateGate;
        readonly UdpClient udp;
        readonly IPEndPoint listenEP, sendEP;
        readonly HashSet<Func<PublishPacket, Task>> callbacks = new HashSet<Func<PublishPacket, Task>>();
        readonly Timer pingTimer;
        readonly ILogger<Server>? logger;

        public Server(IPEndPoint endpoint, ILogger<Server>? logger = null, (int occurrences, TimeSpan timeUnit) limitter = default)
        {
            this.logger = logger;

            if (limitter != default) RateGate = new RateGate(limitter.occurrences, limitter.timeUnit);

            listenEP = new IPEndPoint(IPAddress.Any, endpoint.Port);
            sendEP = endpoint;

            var address = endpoint.Address;

            udp = new UdpClient();
            udp.ExclusiveAddressUse = false;

            if (address == IPAddress.Broadcast)
            {
                udp.EnableBroadcast = true;
            }
            else if (address.IsIpv4MulticastAddress() || address.IsIPv6Multicast)
            {
                udp.JoinMulticastGroup(address);
            }

            udp.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
            udp.Client.Bind(listenEP);

            pingTimer = new Timer(_ => _ = Ping(), null, TimeSpan.FromSeconds(0), TimeSpan.FromSeconds(60));
        }

        public async Task Subscribe(string topic)
        {
            topics.Add(new TopicFilter(topic));
            var data = PacketEncoder.EncodeSubscribe(new SubscribePacket(topic)).ToArray();
            await SendAsync(data);
        }

        public Task Unsubscribe(string topic)
        {
            topics.RemoveAll(x => x.filter == topic);
            return Task.CompletedTask;
        }

        public Task Ping()
        {
            logger?.LogTrace("TX PING");
            return SendAsync(new[] { (byte)PacketType.PingReq, (byte)0x00 });
        }

        public async Task Publish(PublishPacket message, PublishOptions? options = null)
        {
            options = options ?? DefaultPublishOptions;
            var bytes = PacketEncoder.EncodePublish(message, options).ToArray();
            if (bytes.Length > MaxSize) throw new InvalidOperationException($"Encoded packet lenght of {bytes.Length} exceeds configured maximum of {MaxSize}. Consider increasing this if you know your network is reliable.");

            if (options.Retain)
            {
                logger?.LogTrace("Retaining message");
                retainedMessages[message.Topic] = bytes;
            }

            var items = new List<(IPEndPoint, int, byte[])>();

            await SendAsync(bytes);
        }

        protected async Task SendAsync(byte[] bytes)
        {
            if (RateGate != null) await RateGate.WaitAsync();
            await udp.SendAsync(bytes, bytes.Length, sendEP);
        }

        public void AddSink(Func<PublishPacket, Task> callback)
        {
            callbacks.Add(callback);
        }

        public async void StartListening()
        {
            while (true)
            {
                try
                {
                    var result = await udp.ReceiveAsync();
                    var bytes = result.Buffer;

                    if (bytes.Length < 2) throw new InvalidOperationException("Packet lenght < 2");

                    switch (bytes[0])
                    {
                        case (byte)PacketType.PingReq:
                            logger?.LogTrace("RX PING: {0}, Sending PONG", result.RemoteEndPoint);
                            _ = SendAsync(new[] { (byte)PacketType.PingResp, (byte)0x00 });
                            break;
                        case (byte)PacketType.PingResp:
                            logger?.LogTrace("RX PONG: {0}", result.RemoteEndPoint);
                            break;
                        case (byte)PacketType.Publish:
                            var publish = PacketEncoder.DecodePublish(bytes);

                            if (topics.Any(x => x.IsMatch(publish.Topic)))
                            {
                                foreach (var callback in callbacks) _ = callback(publish);
                            }
                            else
                            {
                                logger?.LogTrace("Received message but no topics that match on: {0}", publish.Topic);
                            }
                            break;
                        case (byte)PacketType.Subscribe:
                            var subscribe = PacketEncoder.DecodeSubscribe(bytes);
                            logger?.LogTrace($"Subscriber for: {subscribe.Topic}");
                            foreach (var i in retainedMessages)
                            {
                                logger?.LogTrace($"Comparing with: {i.Key}");
                                var filter = new TopicFilter(subscribe.Topic);
                                if (filter.IsMatch(i.Key))
                                {
                                    _ = SendAsync(i.Value);
                                }
                            }
                            break;
                    }
                }
                catch (Exception ex)
                {
                    logger?.LogError(ex, "Process failed");
                }
            }
        }
    }
}
