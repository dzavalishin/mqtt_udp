using System;
using System.Security.Cryptography;
using System.Text;

namespace MqttUdp
{
    public static class PublishPacketExtensions
    {
        public static string ReadString(this PublishPacket instance)
        {
            Guard.IsNotNull(instance, nameof(instance));
            return Encoding.UTF8.GetString(instance.Payload);
        }
        public static void Write(this PublishPacket instance, string value)
        {
            Guard.IsNotNull(instance, nameof(instance));
            instance.Payload = Encoding.UTF8.GetBytes(value);
        }

        public static bool IsHashValid(this PublishPacket instance)
        {
            Guard.IsNotNull(instance, nameof(instance));
            using var md5Hash = MD5.Create();
            var hashCalculated = md5Hash.ComputeHash(instance.Payload).AsSpan();
            return hashCalculated.SequenceEqual(instance.Hash);
        }
    }
}
