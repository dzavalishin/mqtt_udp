package dz.ru.mqtt_udp.kt;

import dz.ru.mqtt_udp.kt.io.UdpIO

public class Engine()
{
    private var signatureRequired : Boolean = false;
    private var signatureKey : String = "signPassword"; // TODO empty?

    public fun isSignatureRequired(): Boolean {        return signatureRequired    }
    public fun setSignatureRequired(req: Boolean) {        signatureRequired = req    }
    public fun getSignatureKey(): String {        return signatureKey    }
    public fun setSignatureKey(key: String) {        signatureKey = key    }


    private val t = Throttle()
    private val io = UdpIO()

    private val r = PacketReceiver(io);
    fun start() {
        r.requestStart()
    }

    fun setThrottle( msec : Int ) { t.setThrottle(msec); }


    private var maxReplyQoS = 1 // By default - TODO - add to defs!

    public fun getMaxReplyQoS(): Int {        return maxReplyQoS    }
    public fun setMaxReplyQoS(newMax: Int) {        maxReplyQoS = newMax    }


    companion object {
        }

}