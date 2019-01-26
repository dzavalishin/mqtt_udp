/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * Speed limit
 *
 *
**/


#include "config.h"

//#include <time.h>
//#include <unistd.h> // sleep()

#include "mqtt_udp.h"

/**
 *
 * NB!
 *
 * No sub-second timers are used. Therefore we calculate max_seq_packets dynamically
 * to match one second interval.
 *
**/



static uint64_t           last_send_time = 0;
static volatile uint64_t  last_send_count = 0;



/**
 * up to 3 packets can be sent with no throttle
 * most devices have some buffer and we do not
 * want to calc time each send
 **/
static int max_seq_packets = 10;

/**
 * Time between outgoing packets
 */
static int throttle = 100;


/**
 * Set packet send rate.
 * @param msec average time in milliseconds between packets. Set to 0 to turn throttling off.
 */
void mqtt_udp_set_throttle(int msec)
{
    throttle = msec;

    if( throttle <= 0 )
    {
        throttle = 0;
        max_seq_packets = 100;
        return;
    }

    max_seq_packets = 1000/msec;
    if(max_seq_packets < 1) max_seq_packets = 1;
}



/**
 * Must be called in packet send code.
 * Will put caller asleep to make sure packets are sent in a right pace.
 */
void mqtt_udp_throttle()
{

    if( throttle == 0 )
        return;

    // Let max_seq_packets come through with no pause.

    uint64_t last_send_count_delta = ++last_send_count;
    if( last_send_count_delta < max_seq_packets )
        return;

    last_send_count -= max_seq_packets; // eat out

    uint64_t now = mqtt_udp_arch_get_time_msec();
    //print( str(now) )
    uint64_t since_last_pkt = now - last_send_time;

    if( last_send_time == 0 )
    {
        last_send_time = now;
        return;
    }

    last_send_time = now;

    uint64_t towait = max_seq_packets * throttle - since_last_pkt;

    // print( str(towait) )

    if( towait <= 0 )
        return;

    // TODO autoconf me in
    //usleep(1000L*towait);
    //sleep(1);
    //mqtt_udp_arch_sleep_msec( towait );

}




