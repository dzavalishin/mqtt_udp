namespace MqttUdp
{
    enum PacketType : byte
    {
        Unknown     = 0,
        Publish     = 0x30,
        Subscribe   = 0x80,
        PingReq     = 0xC0,
        PingResp    = 0xD0,
    }
}
