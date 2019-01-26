/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Generalized MQTT/UDP packet parser
 *
**/

#include "config.h"

//#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
//#include <locale.h>
#include <errno.h>

#include "mqtt_udp.h"


static size_t mqtt_udp_decode_size( const char **pkt );
static size_t mqtt_udp_decode_topic_len( const char *pkt );


// -----------------------------------------------------------------------
// parse
// -----------------------------------------------------------------------



#define MQTT_UDP_PKT_HAS_ID(pkt)  ((pkt.pflags) & 0x6)


// sanity check
#define MAX_SZ (4*1024)
#define CHEWED (pkt - pstart)



int mqtt_udp_parse_any_pkt( const char *pkt, size_t plen, int from_ip, process_pkt callback )
{
    int err = 0;

    struct mqtt_udp_pkt o;
    const char *pstart = pkt;


    //if( plen <= 2 )
    if( plen < 2 )
        return mqtt_udp_global_error_handler( MQ_Err_Proto, -1, "packet len < 2", "" );

    mqtt_udp_clear_pkt( &o );

    o.from_ip = from_ip;

    o.ptype = *pkt++;
    o.pflags = o.ptype & 0xF;
    o.ptype &= 0xF0;


    o.total = mqtt_udp_decode_size( &pkt );

    if( o.total+2 > plen )        
        return mqtt_udp_global_error_handler( MQ_Err_Proto, -2, "packet too short", "" );

    //const char *end_hdr = pkt; // end of header, start of payload
    const char *ttrs_start = pkt+o.total; // end of payload, start of TTRs

    // NB! MQTT/UDP does not use variable header == ID field
    /*
    if(MQTT_UDP_FLAGS_HAS_ID(o.pflags))
    {
        o.pkt_id = (pkt[0] << 8) | pkt[1];
        pkt += 2;
        o.total -= 2;
    }
    else
        o.pkt_id = 0;
    */

    o.topic = o.value = 0;
    o.topic_len = o.value_len = 0;


    // Packets with topic?
    switch( o.ptype )
    {
    case PTYPE_SUBSCRIBE:
    case PTYPE_PUBLISH:
        break;

    default:
        goto parse_ttrs;
    }

    size_t tlen = mqtt_udp_decode_topic_len( pkt );
    pkt += 2;

    if( tlen > MAX_SZ )
        return mqtt_udp_global_error_handler( MQ_Err_Proto, -3, "packet too long", "" );

    if( CHEWED + tlen > o.total + 2 )
        return mqtt_udp_global_error_handler( MQ_Err_Proto, -4, "packet topic len > pkt len", "" );

    o.topic = malloc( tlen+2 );
    if( o.topic == 0 ) return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );
    strlcpy( o.topic, pkt, tlen+1 );
    o.topic_len = strnlen( o.topic, MAX_SZ );


    pkt += tlen;

    size_t vlen = o.total - CHEWED + 2;
    if( vlen > MAX_SZ )
        return mqtt_udp_global_error_handler( MQ_Err_Proto, -5, "packet value len > pkt len", "" );

    // Packet with value?
    if( o.ptype != PTYPE_PUBLISH )
        goto parse_ttrs;

    vlen++; // strlcpy needs place for zero
    o.value = malloc( tlen+2 );
    if( o.value == 0 )
    {
        free( o.topic );
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );
    }
    strlcpy( o.value, pkt, vlen );
    o.value_len = strnlen( o.value, MAX_SZ );

parse_ttrs:
    ;
    const char *ttrs = ttrs_start;
    int ttrs_len = plen - (ttrs-pstart);

    //printf("TTRs  len=%d, plen=%d\n", ttrs_len, plen );

    while( ttrs_len > 0 )
    {
        const char *ttr_start = ttrs;

        char ttr_type = *ttrs++;
        int  ttr_len = mqtt_udp_decode_size( &ttrs );

        if( ttr_len <= 0 )
        {
            err = mqtt_udp_global_error_handler( MQ_Err_Proto, -6, "TTR len < 0", "" );
            goto cleanup;
        }

        //printf("TTR type = %c 0x%X len=%d\n", ttr_type, ttr_type, ttr_len );

        ttrs_len -= ttrs - ttr_start; // type & len fields
        ttrs_len -= ttr_len;          // TTR data

        ttrs += ttr_len;

        if( ttrs_len < 0 )
        {
            err = mqtt_udp_global_error_handler( MQ_Err_Proto, -6, "TTRs len < 0", "" );
            goto cleanup;
        }
    }


    mqtt_udp_recv_reply( &o );
    callback( &o );

cleanup:
    if( o.topic ) free( o.topic );
    if( o.value ) free( o.value );

    return err;
}






static size_t mqtt_udp_decode_size( const char **pkt )
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

static size_t mqtt_udp_decode_topic_len( const char *pkt )
{
    return (pkt[0] << 8) | pkt[1];
}


// -----------------------------------------------------------------------
//
// dump
//
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
    return 0;
}



