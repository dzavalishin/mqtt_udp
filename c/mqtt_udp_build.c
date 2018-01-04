/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Generalized MQTT/UDP packet builder
 *
**/

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <locale.h>
#include <errno.h>

#include "mqtt_udp.h"

static int pack_len( char *buf, int *blen, int *used, int data_len );

//static size_t mqtt_udp_decode_size( char **pkt );
//static size_t mqtt_udp_decode_topic_len( const unsigned char *pkt );


// -----------------------------------------------------------------------
//
// -----------------------------------------------------------------------



#define MQTT_UDP_PKT_HAS_ID(pkt)  ((pkt.pflags) & 0x6)


// sanity check
//#define MAX_SZ (4*1024)
//#define CHEWED (pkt - pstart)

#define MQTT_UDP_PKT_HAS_ID(pkt)  ((pkt->pflags) & 0x6)


int mqtt_udp_build_any_pkt( const char *buf, size_t blen, struct mqtt_udp_pkt *p )
{
    // TODO check for consistency - if pkt has to have topic & value and has it

    int tlen = p->topic ? p->topic_len : 0;
    int dlen = p->value ? p->value_len : 0;

    unsigned char *bp = buf;

    *bp++ = (p->ptype & 0xF0) | (p->pflags & 0x0F);
    blen--;

    // TODO incorrect
    int total = tlen + dlen + 2 + 2; // packet size
    if( MQTT_UDP_PKT_HAS_ID(p) ) total += 2;

    if( total > blen )
        return ENOMEM;

    //int size = total+1;

    int used = 0;
    int rc = pack_len( bp, &blen, &used, total );
    if( rc ) return rc;

    bp += used;

    if( MQTT_UDP_PKT_HAS_ID(p) )
    {
        *bp++ = (p->pkt_id >> 8) & 0xFF;
        *bp++ = p->pkt_id & 0xFF;
        blen -= 2;
    }

    if( tlen )
    {
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

    }

}










// -----------------------------------------------------------------------
// Bits
// -----------------------------------------------------------------------



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




