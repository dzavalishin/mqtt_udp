/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Packet send
 *
**/

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#include "mqtt_udp.h"

#define BUFLEN PKT_BUF_SIZE // 512



static int pack_len( char *buf, int *blen, int *used, int data_len )
{
    *used = 0;
    while( 1 )
    {
        if( *blen <= 0 ) return ENOMEM;

        int byte = data_len % 128;
        data_len /= 128;

        if( data_len > 0 )
            byte |= 0x80;

        *buf++ = byte;
        (*blen)--;
        (*used)++;

        if( data_len == 0 ) return 0;
    }
}

// ----------------------------------------------------
// Make and send PUBLISH packet
// ----------------------------------------------------
//int mqtt_udp_send( int fd, char *topic, char *data )
//{
//    return mqtt_udp_send_publish( fd, topic, data );
//}

int mqtt_udp_send_publish( int fd, char *topic, char *data )
{
    struct mqtt_udp_pkt p;
    unsigned char buf[BUFLEN];
    int rc;
    size_t out_size;

    mqtt_udp_clear_pkt( &p );

    p.ptype = PTYPE_PUBLISH;
    p.topic = topic;
    p.value = data;
    p.topic_len = strlen( topic );
    p.value_len = strlen( data );

    mqtt_udp_dump_any_pkt( &p );

    rc = mqtt_udp_build_any_pkt( buf, BUFLEN, &p, &out_size );
    if( rc ) return rc;

    mqtt_udp_dump( buf, out_size );

    return mqtt_udp_send_pkt( fd, buf, out_size );
}


// ----------------------------------------------------
// Packet with no payload, just type and zero length
// ----------------------------------------------------

static int mqtt_udp_send_empty_pkt( int fd, char ptype )
{
    unsigned char buf[2];
    buf[0] = ptype;
    buf[1] = 0;
    return mqtt_udp_send_pkt( fd, buf, sizeof(buf) );
}


// ----------------------------------------------------
// Ping
// ----------------------------------------------------


int mqtt_udp_send_ping_request( int fd )
{
    return mqtt_udp_send_empty_pkt( fd, PTYPE_PINGREQ );
}


//int mqtt_udp_send_ping_responce( int fd, int ip_addr )
int mqtt_udp_send_ping_responce( int fd )
{
    return mqtt_udp_send_empty_pkt( fd, PTYPE_PINGRESP );
}









