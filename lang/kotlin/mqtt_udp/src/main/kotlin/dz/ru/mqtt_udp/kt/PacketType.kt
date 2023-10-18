package dz.ru.mqtt_udp.kt

enum class PacketType(val ordinal: Int) {
    Unknown(0),
    Publish(0x30), // TODO use Defs
    Subscribe(0x80),
    Ping(0xC0),
    PingResponce(0xD0),

}