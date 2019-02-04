/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Packet send
 * 
 * Build packet structure, pass to encode and send out.
 * 
 * Can be used even if reception is not started.
 *
**/

#include "config.h"

#include <string.h>
//#include <stdio.h>
#include <stdlib.h>

#include "mqtt_udp.h"





// ----------------------------------------------------
// Make and send PUBLISH packet
// ----------------------------------------------------

/**
 * 
 * @brief Compose and send PUBLISH packet.
 * 
 * @param topic  Message topic
 * @param data   Message value, usually text string
 * 
 * @returns 0 if ok, or error code
**/
int mqtt_udp_send_publish( char *topic, char *data )
{
    struct mqtt_udp_pkt p;
    char buf[PKT_BUF_SIZE];
    int rc;
    size_t out_size;

    mqtt_udp_clear_pkt( &p );

    p.ptype = PTYPE_PUBLISH;
    p.topic = topic;
    p.value = data;
    p.topic_len = strnlen( topic, PKT_BUF_SIZE );
    p.value_len = strnlen( data, PKT_BUF_SIZE );

    //mqtt_udp_dump_any_pkt( &p );

    rc = mqtt_udp_build_any_pkt( buf, PKT_BUF_SIZE, &p, &out_size );
    if( rc ) return rc;

    //mqtt_udp_dump( buf, out_size );

    return mqtt_udp_send_pkt( mqtt_udp_get_send_fd(), buf, out_size );
}


// ----------------------------------------------------
// Make and send SUBSCRIBE packet
// ----------------------------------------------------


/**
 * 
 * @brief Compose and send SUBSCRIBE packet.
 * 
 * @param topic  Message topic
 * 
 * @returns 0 if ok, or error code
**/
int mqtt_udp_send_subscribe( char *topic )
{
    struct mqtt_udp_pkt p;
    char buf[PKT_BUF_SIZE];
    int rc;
    size_t out_size;

    char qos = 0;

    mqtt_udp_clear_pkt( &p );

    p.ptype = PTYPE_SUBSCRIBE;
    p.topic = topic;
    p.value = &qos;
    p.topic_len = strnlen( topic, PKT_BUF_SIZE );
    p.value_len = 1;

    //mqtt_udp_dump_any_pkt( &p );

    rc = mqtt_udp_build_any_pkt( buf, PKT_BUF_SIZE, &p, &out_size );
    if( rc ) return rc;

    //mqtt_udp_dump( buf, out_size );

    return mqtt_udp_send_pkt( mqtt_udp_get_send_fd(), buf, out_size );
}


// ----------------------------------------------------
// Packet with no payload, just type and zero length
// ----------------------------------------------------

/// Send empty packet of given type
/// @param ptype Packet type
static int mqtt_udp_send_empty_pkt( char ptype )
{
    char buf[2];
    buf[0] = ptype;
    buf[1] = 0;
    return mqtt_udp_send_pkt( mqtt_udp_get_send_fd(), buf, sizeof(buf) );
}


// ----------------------------------------------------
// Ping
// ----------------------------------------------------

/// Send PINGREQ message
int mqtt_udp_send_ping_request( void )
{
    return mqtt_udp_send_empty_pkt( PTYPE_PINGREQ );
}


/// Send PINGRESP message
int mqtt_udp_send_ping_responce( void )
{
    return mqtt_udp_send_empty_pkt( PTYPE_PINGRESP );
}









