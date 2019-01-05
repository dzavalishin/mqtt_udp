/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Generalized MQTT/UDP packet parser
 *
**/

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <locale.h>
#include <errno.h>

#include "mqtt_udp.h"


static size_t mqtt_udp_decode_size( char **pkt );
static size_t mqtt_udp_decode_topic_len( const unsigned char *pkt );


// -----------------------------------------------------------------------
// parse
// -----------------------------------------------------------------------



#define MQTT_UDP_PKT_HAS_ID(pkt)  ((pkt.pflags) & 0x6)


// sanity check
#define MAX_SZ (4*1024)
#define CHEWED (pkt - pstart)



int mqtt_udp_parse_any_pkt( const char *pkt, size_t plen, int from_ip, process_pkt callback )
{
    struct mqtt_udp_pkt o;
    const char *pstart = pkt;


    if( plen <= 2 )        return -1;

    mqtt_udp_clear_pkt( &o );

    o.from_ip = from_ip;

    o.ptype = *pkt++;
    o.pflags = o.ptype & 0xF;
    o.ptype &= 0xF0;


    o.total = mqtt_udp_decode_size( (char **)&pkt );

    if( o.total+2 > plen )        return -2;

    //if( MQTT_UDP_PKT_HAS_ID(o) )
    if(MQTT_UDP_FLAGS_HAS_ID(o.pflags))
    {
        o.pkt_id = (pkt[0] << 8) | pkt[1];
        pkt += 2;
        o.total -= 2;
    }
    else
        o.pkt_id = 0;


    o.topic = o.value = 0;
    o.topic_len = o.value_len = 0;


    switch( o.ptype )
    {
    case PTYPE_SUBSCRIBE:
    case PTYPE_PUBLISH:
        break;

    default:
        goto done;
    }

    size_t tlen = mqtt_udp_decode_topic_len( pkt );
    pkt += 2;

    if( tlen > MAX_SZ )
        return -3;

    if( CHEWED + tlen > o.total + 2 )
        return -4;

    o.topic = malloc( tlen+2 );
    if( o.topic == 0 ) return ENOMEM;
    strlcpy( o.topic, pkt, tlen+1 );
    o.topic_len = strnlen( o.topic, MAX_SZ );


    pkt += tlen;

    size_t vlen = o.total - CHEWED + 2;
    if( vlen > MAX_SZ )
        return -5;

    if( o.ptype != PTYPE_PUBLISH )
        goto done;

    vlen++; // strlcpy needs place for zero
    o.value = malloc( tlen+2 );
    if( o.value == 0 )
    {
        free( o.topic );
        return ENOMEM;
    }
    strlcpy( o.value, pkt, vlen );
    o.value_len = strnlen( o.value, MAX_SZ );

done:

    mqtt_udp_recv_reply( &o );
    callback( &o );

    if( o.topic ) free( o.topic );
    if( o.value ) free( o.value );

    return 0;
}






static size_t mqtt_udp_decode_size( char **pkt )
{
    size_t ret = 0;

    while(1)
    {
        unsigned char byte = **pkt; (*pkt)++;
        ret |= byte & ~0x80;

        if( (byte & 0x80) == 0 )
            return ret;

        ret <<= 7;
    }
}

static size_t mqtt_udp_decode_topic_len( const unsigned char *pkt )
{
    return (pkt[0] << 8) | pkt[1];
}


// -----------------------------------------------------------------------
// dump
// -----------------------------------------------------------------------


static char *ptname[] =
{
    "?0x00",
    "CONNECT",	"CONNACK", 	"PUBLISH", 	"PUBACK",
    "PUBREC",   "PUBREL",       "PUBCOMP",      "SUBSCRIBE",
    "SUBACK",   "UNSUBSCRIBE",  "UNSUBACK",     "PINGREQ",
    "PINGRESP", "DISCONNECT",
    "?0xF0"
};


int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o )
{
    const char *tn = ptname[ o->ptype >> 4 ];

    printf( "pkt %s flags %x, id %d from %d.%d.%d.%d",
            tn, o->pflags, o->pkt_id,
            0xFF & (o->from_ip >> 24),
            0xFF & (o->from_ip >> 16),
            0xFF & (o->from_ip >> 8),
            0xFF & (o->from_ip)
          );

    if( o->topic_len > 0 )
        printf(" topic '%s'", o->topic );

    if( o->value_len > 0 )
        printf(" = '%s'", o->value );

    printf( "\n");
}



