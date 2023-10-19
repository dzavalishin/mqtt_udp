package dz.ru.mqtt_udp.kt

interface IPacketListener {
    abstract fun accept(packet: Packet);
}