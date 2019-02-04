/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * 
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief MQTT/UDP packet structure handling.
 *
**/

#include "config.h"

#include <string.h>
#include <stdlib.h>

#include "mqtt_udp.h"



// -----------------------------------------------------------------------
// Clear
// -----------------------------------------------------------------------

/// Clear packet
void mqtt_udp_clear_pkt( struct mqtt_udp_pkt *p )
{
    memset( p, 0, sizeof(struct mqtt_udp_pkt) );
}


// -----------------------------------------------------------------------
// Release memory
// -----------------------------------------------------------------------

/// Release memory used by this packet, but not packet structure itself.
void mqtt_udp_free_pkt( struct mqtt_udp_pkt *p )
{
    if( p->topic ) free( p->topic );
    p->topic = 0;

    if( p->value ) free( p->value );
    p->value = 0;

}


