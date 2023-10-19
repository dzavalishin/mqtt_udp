package dz.ru.mqtt_udp.kt

import dz.ru.mqtt_udp.kt.io.UdpIO
import dz.ru.mqtt_udp.kt.util.LoopRunner
import dz.ru.mqtt_udp.kt.util.MqttProtocolException
import java.io.IOException


class PacketReceiver(val io : UdpIO) : LoopRunner("MQTT/UDP input") {


    // ------------------------------------------------------------
    // Replies on/off
    // ------------------------------------------------------------
    private var muted = false
    fun isMuted(): Boolean {
        return muted
    }

    /**
     * Set muted mode. In muted mode server loop won't respond to any incoming packets
     * (such as PINGREQ) automatically.
     *
     * @param muted If true - mute replies.
     */
    fun setMuted(muted: Boolean) {
        this.muted = muted
    }


    private val listeners : MutableList<IPacketListener> = mutableListOf(); // MutableList<IPacketListener>();
    public fun addListener(l : IPacketListener) {
        listeners.add(l)
    }

    private fun processPacket(p : Packet )
    {
        listeners.forEach{ it.accept(p) }

    }


    @Throws(IOException::class, MqttProtocolException::class)
    override fun onStart() {
        io.startInput();
    }

    @Throws(IOException::class, MqttProtocolException::class)
    override fun step() {
        val p: Packet = io.input()
        if (!muted) preprocessPacket(p)
        processPacket(p)
    }

    @Throws(IOException::class, MqttProtocolException::class)
    override fun onStop() {
        io.stopInput()
    }


    /**
     * Does internal protocol defined packet processing.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun preprocessPacket(p: Packet) {

        //if (p instanceof PublishPacket) {		} else
        if (p is PingReqPacket) {
            // Reply to ping
            val presp = PingRespPacket()
            //presp.send(ss, ((PingReqPacket) p).getFrom().getInetAddress());
            // decided to broadcast ping replies
            presp.send(ss)
            io.se
        } else if (p is PublishPacket) {
            val pp: PublishPacket = p as PublishPacket
            var qos: Int = pp.getQoS()
            if (qos != 0) {
                System.out.println("QoS, Publish id=" + pp.getPacketNumber().orElse(0))
                val maxQos: Int = Engine.getMaxReplyQoS()
                qos = Integer.min(qos, maxQos)
                PubAckPacket(pp, qos).send()
            }
        }
    }

}