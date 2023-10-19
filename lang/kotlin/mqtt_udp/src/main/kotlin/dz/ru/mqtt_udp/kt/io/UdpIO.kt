package dz.ru.mqtt_udp.kt.io

import dz.ru.mqtt_udp.kt.Packet
import java.net.*

class UdpIO {
    fun send( data : ByteArray ) {
        val s = ss.s
        val pkt = DatagramPacket(data, data.size)
        s.send(pkt)
    }

    fun startInput() {
        TODO("Not yet implemented")
    }

    fun stopInput() {
        TODO("Not yet implemented")
    }

    fun input(): Packet {
        TODO("Not yet implemented")
    }


}


private object ss  {
    val s = DatagramSocket()
    init {
        s.connect(Inet4Address.getByName("255.255.255.255"), 1883) // TODO use defs
    }
};