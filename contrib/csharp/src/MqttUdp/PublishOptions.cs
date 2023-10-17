namespace MqttUdp
{
    public class PublishOptions
    {
        public bool AddSequenceNumber { get; set; }
        public bool AddSignature { get; set; }
        public bool AddSentTime { get; set; }
        public bool Retain { get; set; }
    }
}
