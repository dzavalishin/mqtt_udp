using System;
using System.Collections;

namespace MqttUdp
{
    public class PublishPacket : Packet
    {
        public string Topic { get; set; } = string.Empty;
        public byte[] Payload { get; set; } = Array.Empty<byte>();
    }
}
