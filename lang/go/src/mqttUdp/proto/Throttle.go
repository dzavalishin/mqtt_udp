package proto

import "time"

/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Speed limit
 *
 * Limits number of outgoing packets per time interval.
 *
**/

/**
 *
 * NB!
 *
 * In Unix no sub-second timers are used. Therefore we calculate max_seq_packets dynamically
 * to match one second interval.
 *
**/

var last_send_time time.Time = time.Now() // TODO must assign some invalid value
var last_send_count uint64 = 0

/**
 * Up to 3 packets can be sent with no throttle
 * most devices have some buffer and we do not
 * want to calc time each send.
 **/
var max_seq_packets uint64 = 10

/**
 * Time between outgoing packets
 */
var throttle uint64 = 100

/**
 *
 * Set packet send rate.
 *
 * @param msec average time in milliseconds between packets. Set to 0 to turn throttling off.
 *
 */
func SetThrottle(msec int) {
	throttle = uint64(msec)

	if throttle <= 0 {
		throttle = 0
		max_seq_packets = 100
		return
	}

	max_seq_packets = uint64(1000 / msec)
	if max_seq_packets < 1 {
		max_seq_packets = 1
	}
}

/*

Must be called in packet send code.

 Will put caller asleep to make sure packets are sent in a right pace.

*/
func Throttle() {

	if throttle == 0 {
		return
	}

	// Let max_seq_packets come through with no pause.
	last_send_count++

	var last_send_count_delta uint64 = last_send_count

	if last_send_count_delta < max_seq_packets {
		return
	}

	last_send_count -= max_seq_packets // eat out

	var now time.Time = arch_get_time_msec()

	var since_last_pkt time.Duration = now.Sub(last_send_time) // now - last_send_time
	//printf("\n\nsince_last_pkt %lld msec, mult=%d\n", since_last_pkt, max_seq_packets * throttle );

	/*
		// TODO right?
		if last_send_time == 0 {
			last_send_time = now
			return
		} */

	last_send_time = now

	var towait uint64 = uint64(max_seq_packets*throttle - uint64(since_last_pkt.Milliseconds()))

	//printf("\nthrottle sleep %lld msec\n\n", towait );
	if towait <= 0 {
		return
	}

	// TODO autoconf me in
	//usleep(1000L*towait);

	arch_sleep_msec(towait)

}

func arch_sleep_msec(towait uint64) {
	time.Sleep(time.Duration(towait) * time.Millisecond)
}

func arch_get_time_msec() time.Time {
	return time.Now()
}
