/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * Architecture and OS dependent time related code.
 *
 *
**/


//#include "../../config.h"
#include "config.h"

#include <time.h>
#include <unistd.h> // sleep()

#include "../../mqtt_udp.h"


uint64_t mqtt_udp_arch_get_time_msec()
{
    uint64_t ret = time(0);
    return 1000LL * ret;
}

void  mqtt_udp_arch_sleep_msec( uint32_t msec )
{
    // have only sleep
    int sec = ((msec-1)/1000)+1;
    sleep( sec );
}


