using System;
using System.Security.Cryptography;
using System.Text;
using System.Threading;

namespace MqttUdp
{
    internal static partial class PacketEncoder
    {
        static readonly Encoding Encoding = Encoding.UTF8;
        static int sequence;
        public static Func<long> Utcnow = () => DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

        public static Span<byte> EncodePublish(PublishPacket packet, PublishOptions options)
        {
            //Packet type and flags: 0x30
            //Remaining length (1-2 bytes)
            //NO Packet id
            //Topic length (2 bytes)
            //Topic name, UTF-8 string
            // Value, UTF-8 string
            //var sw = new BinaryWriter(s, Encoding.UTF8);

            var w = new Writer();

            var topicBytes = Encoding.GetBytes(packet.Topic);
            const int topicLengthBytesLength = 2;
            w.Write((byte)PacketType.Publish);

            var length = topicLengthBytesLength + topicBytes.Length + packet.Payload.Length;

            w.WriteVlq(length);
            w.WriteBigEndian((short)topicBytes.Length);
            w.Write(topicBytes);
            w.Write(packet.Payload);

            // TTR - Reply at
            if (packet.ReplyTo != default)
            {
                w.Write((byte)'r');
                w.Write((byte)sizeof(int));
                w.WriteBigEndian(packet.ReplyTo);
            }

            // TTR - Measured at
            if (packet.MeasuredAt != default)
            {
                w.Write((byte)'m');
                w.Write((byte)8);
                w.WriteBigEndian(new DateTimeOffset(packet.MeasuredAt).ToUnixTimeMilliseconds());
            }

            if (options.AddSequenceNumber)
            {
                // TTR - Sequence number
                var number = Interlocked.Increment(ref sequence);
                w.Write((byte)'n');
                w.Write((byte)sizeof(int));
                w.WriteBigEndian(number);
                packet.Sequence = number;
            }

            if (options.AddSentTime)
            {
                var sentAt = Utcnow();
                // TTR - Sent at
                w.Write((byte)'p');
                w.Write((byte)sizeof(long));
                w.WriteBigEndian(sentAt);
                packet.SentAt = DateTimeOffset.FromUnixTimeMilliseconds(sentAt).UtcDateTime;
            }

            if (options.AddSignature)
            {
                // TTR - Signature
                var dataLength = w.Position;
                w.Write((byte)'s');
                w.Write((byte)16);
                using (var md5Hash = MD5.Create())
                {
                    md5Hash.TryComputeHash(w.Buffer.AsSpan(0, dataLength), w.Buffer.AsSpan(w.Position, 16), out _);
                }
                packet.Hash = w.Buffer.AsSpan(w.Position, 16).ToArray();
                w.Position += 16;
            }

            return w.Buffer.AsSpan(0, w.Position);
        }

        public static PublishPacket DecodePublish(byte[] data)
        {
            var r = new Reader(data);

            var type = r.ReadByte();
            Guard.Assert((byte)PacketType.Publish, type, "Type");

            var length = r.ReadInt32Vlq(out _);

            if (length + 2 > data.Length) throw new InvalidOperationException("Packet too short");

            const int topicLengthBytes = 2;

            var topicLength = r.ReadInt16BigEndian();
            var topic = Encoding.GetString(r.ReadBytes(topicLength));
            var remaining = length - topicLengthBytes - topicLength;
            var payload = r.ReadBytes(remaining);

            var result = new PublishPacket
            {
                Topic = topic,
                Payload = payload.ToArray(),
            };

            ProcessTTR(r, result);

            Guard.Assert(r.Position, data.Length);

            return result;
        }

        public static Span<byte> EncodeSubscribe(SubscribePacket packet)
        {
            var w = new Writer();
            w.Write((byte)PacketType.Subscribe);

            var topicBytes = Encoding.GetBytes(packet.Topic);
            var length = topicBytes.Length + 2;

            w.WriteVlq(length);

            //Topic
            w.WriteBigEndian((short)topicBytes.Length);
            w.Write(topicBytes);
            w.Write((byte)packet.QoS);

            return w.Buffer.AsSpan(0, w.Position);
        }

        public static SubscribePacket DecodeSubscribe(byte[] data)
        {
            var r = new Reader(data);
            var type = r.ReadByte();
            Guard.Assert((byte)PacketType.Subscribe, type);

            var length = r.ReadInt32Vlq(out var _);

            if (length + 2 > data.Length) throw new InvalidOperationException("Packet too short");

            var topicLength = r.ReadInt16BigEndian();
            var topic = Encoding.GetString(r.ReadBytes(topicLength));
            var qos = r.ReadByte();

            var result = new SubscribePacket(topic) { QoS = qos };

            ProcessTTR(r, result);

            Guard.Assert(r.Position, data.Length);

            return result;
        }

        private static void ProcessTTR(Reader r, Packet packet)
        {
            while (r.Peek())
            {
                var ttrType = (char)r.ReadByte();
                var ttrLength = r.ReadInt32Vlq(out var _);

                switch (ttrType)
                {
                    case 'r':
                        Guard.Assert(4, ttrLength);
                        packet.ReplyTo = r.ReadInt32BigEndian();
                        break;
                    case 'n':
                        Guard.Assert(4, ttrLength);
                        packet.Sequence = r.ReadInt32BigEndian();
                        break;
                    case 's':
                        Guard.Assert(16, ttrLength);
                        packet.Hash = r.ReadBytes(16).ToArray();
                        break;
                    case 'm'://64 bit integer in network (big endian) byte order.
                        Guard.Assert(sizeof(long), ttrLength);
                        packet.MeasuredAt = DateTimeOffset.FromUnixTimeMilliseconds(r.ReadInt64BigEndian()).UtcDateTime;
                        break;
                    case 'p'://64 bit integer in network (big endian) byte order.
                        Guard.Assert(8, ttrLength);
                        packet.SentAt = DateTimeOffset.FromUnixTimeMilliseconds(r.ReadInt64BigEndian()).UtcDateTime;
                        break;
                    default:
                        Console.WriteLine("Unsupported TTR: {0}", ttrType);
                        r.ReadBytes(ttrLength);
                        break;
                }
            }
        }
    }
}
