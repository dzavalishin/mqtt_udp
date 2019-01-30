/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * 
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * MQTT/UDP packet structure handling
 *
**/

#include "config.h"

#include <string.h>
//#include <stdio.h>
#include <stdlib.h>
//#include <errno.h>

#include "mqtt_udp.h"



// -----------------------------------------------------------------------
// Clear
// -----------------------------------------------------------------------

void mqtt_udp_clear_pkt( struct mqtt_udp_pkt *p )
{
    memset( p, 0, sizeof(struct mqtt_udp_pkt) );
}


// -----------------------------------------------------------------------
// Release memory
// -----------------------------------------------------------------------

void mqtt_udp_free_pkt( struct mqtt_udp_pkt *p )
{
    if( p->topic ) free( p->topic );
    p->topic = 0;

    if( p->value ) free( p->value );
    p->value = 0;

}


