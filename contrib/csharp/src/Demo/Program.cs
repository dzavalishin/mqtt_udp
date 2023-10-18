using MqttUdp;
using System;
using System.Text;
using System.Threading.Tasks;

namespace Demo
{
    class Program
    {
        static async Task Main()
        {
            var s = new MqttUdp.Server(Server.DefaultAddress);

            s.AddSink((pr) =>
            {
                return Console.Out.WriteLineAsync(Encoding.UTF8.GetString(pr.Payload));
            });

            s.StartListening();

            s.DefaultPublishOptions.AddSentTime = true;
            s.DefaultPublishOptions.AddSequenceNumber = true;
            s.DefaultPublishOptions.AddSignature = true;
            s.DefaultPublishOptions.Retain = true;

            await s.Subscribe("test/#");

            var id = Guid.NewGuid();

            while (true)
            {
                var p = new MqttUdp.PublishPacket
                {
                    Topic = "topic/123/aa",
                    Payload = Encoding.UTF8.GetBytes("bytes"),
                    MeasuredAt = DateTime.UtcNow
                };
                p.Topic = $"test/{DateTime.Now.Ticks}";
                p.Payload = Encoding.UTF8.GetBytes(id + "/" + DateTime.Now.ToString());
                await s.Publish(p);
                await Task.Delay(3000);
            }
        }
    }
}
