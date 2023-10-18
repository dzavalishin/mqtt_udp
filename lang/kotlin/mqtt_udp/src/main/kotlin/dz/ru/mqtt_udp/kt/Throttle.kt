package dz.ru.mqtt_udp.kt

import dz.ru.mqtt_udp.kt.util.Sleeper

class Throttle : Sleeper() {
    private var last_send_time: Long = 0
    private var last_send_count : Long = 0;
    private var throttle = 100; // min time between outgoing packets

    /**
     * up to 3 packets can be sent with no throttle
     * most devices have some buffer, and we do not
     * want to calc time each send
     */
    private var max_seq_packets = 3

    fun setThrottle( msec : Int ) {
        throttle = msec;
    }

    /**
     * Must be called in packet send code.
     * Will put caller asleep to make sure packets are sent in a right pace.
     */
    fun throttle() {
        if (throttle == 0) return

        // Let max_seq_packets come through with no pause.
        val last_send_count_delta: Long = last_send_count++
        if (last_send_count_delta < max_seq_packets) return

        //last_send_count.addAndGet(-last_send_count_delta); // eat out
        last_send_count += max_seq_packets
        val now: Long = timeMsec()
        //print( str(now) )
        val since_last_pkt: Long = now - last_send_time
        if (last_send_time == 0L) {
            last_send_time = now
            return
        }
        last_send_time = now
        val towait: Long = max_seq_packets * throttle - since_last_pkt

        // print( str(towait) )
        if (towait <= 0) return
        sleep(towait)
        /*try {
			Thread.sleep(towait);
		} catch (InterruptedException e) {
			GlobalErrorHandler.handleError(ErrorType.Timeout, e);
		}*/
    }

}