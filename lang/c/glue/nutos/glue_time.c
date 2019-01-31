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
#include "../../mqtt_udp.h"

#include <sys/timer.h>


uint64_t mqtt_udp_arch_get_time_msec()
{
    return NutGetMillis();
}

void  mqtt_udp_arch_sleep_msec( uint32_t msec )
{
    NutSleep( msec );
}

