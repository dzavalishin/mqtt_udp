package dz.ru.mqtt_udp.kt;

public class Engine
{

    var signatureRequired : Boolean = false;
    var signatureKey : String = "signPassword"; // TODO empty?

    val t = Throttle()

    constructor() {

    }

    fun start() {
        TODO("Not yet implemented")
    }

    fun setThrottle( msec : int ) { t.setThrottle(msec); }


}