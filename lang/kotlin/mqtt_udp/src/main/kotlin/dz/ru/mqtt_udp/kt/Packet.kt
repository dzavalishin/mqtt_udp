package dz.ru.mqtt_udp.kt

abstract class Packet {
    var type : PacketType = PacketType.Unknown;
    var id: Int = 0;
    var replyTo: Int = 0;


}


