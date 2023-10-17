using NUnit.Framework;

namespace MqttUdp.Tests
{
    public class Tests
    {
        [SetUp]
        public void Setup()
        {
        }

        [Test]
        public void Test1()
        {
            var data = new byte[] {
                0x30, // Packet type
                0x23, // Packet length = 35
                0x00, 0x0e, // Topic length =14
                0x24, 0x53, 0x59, 0x53, 0x2f, 0x6c, 0x6f, 0x63, 0x61, 0x6c, 0x74, 0x69, 0x6d, 0x65, // Topic
                0x32, 0x30, 0x32, 0x30, 0x2f, 0x30, 0x36, 0x2f, 0x31, 0x31, 0x20, 0x31, 0x33, 0x3a, 0x31, 0x38, 0x3a, 0x32, 0x34, //Payload
                0x6e, // 'n'
                0x04, // 4 bytes => uint
                0x00, 0x00, 0x00, 0x00 // uint
                };

            var packet = PacketEncoder.DecodePublish(data);

            Assert.AreEqual("$SYS/localtime", packet.Topic, "Topic");
            Assert.AreEqual(19, packet.Payload.Length, "Payload.Length");
            Assert.AreEqual("2020/06/11 13:18:24", packet.ReadString(), "ReadString");
        }
    }
}