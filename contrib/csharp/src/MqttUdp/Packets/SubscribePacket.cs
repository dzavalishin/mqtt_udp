namespace MqttUdp
{
    public class SubscribePacket : Packet
    {
        public SubscribePacket(string topic)
        {
            Topic = topic;
        }
        public string Topic {get;set;}
        public int QoS {get;set;}
    }
}
