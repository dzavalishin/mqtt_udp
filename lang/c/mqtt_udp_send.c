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

#if 1
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

#else
int mqtt_udp_send_publish( int fd, char *topic, char *data )
{
    unsigned char buf[BUFLEN];

    int tlen = strlen(topic);
    int dlen = strlen(data);

    int blen = sizeof(buf);
    unsigned char *bp = buf;

    *bp++ = PTYPE_PUBLISH;
    blen--;

    int total = tlen + dlen + 2; // packet size
    if( total > blen )
        return ENOMEM;

    //int size = total+1;

    int used = 0;
    int rc = pack_len( bp, &blen, &used, total );
    if( rc ) return rc;

    bp += used;


    *bp++ = (tlen >>8) & 0xFF;
    *bp++ = tlen & 0xFF;
    blen -= 2;

    //NB! Must be UTF-8
    while( tlen-- > 0 )
    {
        if( blen <= 0 ) return ENOMEM;
        *bp++ = *topic++;
        blen--;
    }

    while( dlen-- > 0 )
    {
        if( blen <= 0 ) return ENOMEM;
        *bp++ = *data++;
        blen--;
    }

    return mqtt_udp_send_pkt( fd, buf, bp-buf );
}
#endif

// ----------------------------------------------------
// Packet with no payload, just type and zero length
// ----------------------------------------------------

static int mqtt_udp_send_empty_pkt( int fd, char ptype )
{
#if 1
    unsigned char buf[2];
    buf[0] = ptype;
    buf[1] = 0;
    return mqtt_udp_send_pkt( fd, buf, sizeof(buf) );
#else
    unsigned char buf[BUFLEN];


    int blen = sizeof(buf);
    unsigned char *bp = buf;

    *bp++ = ptype;
    blen--;

    int used = 0;
    int rc = pack_len( bp, &blen, &used, 0 );
    if( rc ) return rc;

    bp += used;

    return mqtt_udp_send_pkt( fd, buf, bp-buf );
#endif
}


// ----------------------------------------------------
// Ping
// ----------------------------------------------------


int mqtt_udp_send_ping_request( int fd )
{
#if 1
    return mqtt_udp_send_empty_pkt( fd, PTYPE_PINGREQ );
#else

    unsigned char buf[BUFLEN];


    int blen = sizeof(buf);
    unsigned char *bp = buf;

    *bp++ = PTYPE_PINGREQ;
    blen--;

    int used = 0;
    int rc = pack_len( bp, &blen, &used, 0 );
    if( rc ) return rc;

    bp += used;

    return mqtt_udp_send_pkt( fd, buf, bp-buf );
#endif
}


int mqtt_udp_send_ping_responce( int fd, int ip_addr )
{
    return mqtt_udp_send_empty_pkt( fd, PTYPE_PINGRESP );
}









