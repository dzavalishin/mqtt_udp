package dz.ru.mqtt_udp.kt.util

open class Sleeper {

    public fun sleep(msec: Long) {
        try {
            Thread.sleep(msec)
        } catch (e: InterruptedException) {
            // Ignore
        }
    }


    public fun timeMsec(): Long {
        return System.currentTimeMillis()
    }

}
