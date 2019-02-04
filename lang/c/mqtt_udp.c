/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Network glue code 
 *
**/


#include "config.h"
#include "mqtt_udp.h"

/// Last used socket for reuse
static int last_socket = -1;

/// Singleton: return one socket to all callers.
int mqtt_udp_get_send_fd( void ) // TODO hack, get fd to send datagrams
{
    if( last_socket < 0 )
        last_socket = mqtt_udp_socket(); // TODO not thread safe
    return last_socket;
}




