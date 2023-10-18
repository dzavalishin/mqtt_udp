using System;

namespace MqttUdp
{
    public abstract class Packet
    {
        public byte Type { get; set; }
        public DateTime MeasuredAt { get; set; }
        public DateTime SentAt { get; set; }
        public int Sequence { get; set; }
        public byte[] Hash { get; internal set; } = Array.Empty<byte>();
        public bool HashMatch { get; set; }
        public int ReplyTo { get; set; }
    }
}

