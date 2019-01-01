/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Reception
 *
**/

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <locale.h>
#include <fcntl.h>
#include <errno.h>

#include "mqtt_udp.h"


// TODO kill me
#if 1

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
    size_t ret = 0;

    ret = (pkt[0] << 8) | pkt[1];

    return ret;
}


#define MAX_SZ (16*1024)
#define CHEWED (pkt - pstart)

/* obsolete
// sanity check

int mqtt_udp_parse_pkt( const char *pkt, size_t plen, char *topic, size_t o_tlen, char *value, size_t o_vlen )
{
    const char *pstart = pkt;

    if( plen <= 4 )
        return EINVAL;

    unsigned char type = *pkt++;

    // lower 4 bits must be 0 - covered below
    //if( (type & 0xF) != 0 )
    //    return EINVAL;

    if( type != PTYPE_PUBLISH )
        return EINVAL;

    size_t total = mqtt_udp_decode_size( (char **)&pkt );

    total += CHEWED;

    if( total > MAX_SZ )
        return EINVAL;

    if( total+1 > plen )
        return EINVAL;

    size_t tlen = mqtt_udp_decode_topic_len( pkt );
    pkt += 2;

    if( tlen > MAX_SZ )
        return EINVAL;

    if( CHEWED + tlen > total )
        return EINVAL;

    //tlen++; // strlcpy needs place for zero
    int tcopy = (tlen+1 > o_tlen) ? o_tlen : tlen+1;
    strlcpy( topic, pkt, tcopy );

    pkt += tlen;

    size_t vlen = total - CHEWED;
    if( vlen > MAX_SZ )
        return EINVAL;

    vlen++; // strlcpy needs place for zero
    int vcopy = (vlen > o_vlen) ? o_vlen : vlen;
    strlcpy( value, pkt, vcopy );

    //printf("total %d tlen %d vlen %d\n", total, tlen, vlen );

    return 0;
}
*/

int mqtt_udp_parse_subscribe_pkt( const char *pkt, size_t plen, char *topic, size_t o_tlen, int *pkt_id_p )
{
    const char *pstart = pkt;

    if( plen <= 4 )
        return EINVAL;

    unsigned char type = *pkt++;

    // lower 4 bits must be 0 - covered below
    //if( (type & 0xF) != 0 )
    //    return EINVAL;

    if( type != PTYPE_SUBSCRIBE )
        return EINVAL;

    size_t total = mqtt_udp_decode_size( (char **)&pkt );

    total += CHEWED;

    if( total > MAX_SZ )
        return EINVAL;

    int pkt_id = (pkt[0] << 8) | pkt[1];
    pkt += 2;
    total -= 2;
    if( pkt_id_p ) *pkt_id_p = pkt_id;


    if( total+1 > plen )
        return EINVAL;

    size_t tlen = mqtt_udp_decode_topic_len( pkt );
    pkt += 2;

    if( tlen > MAX_SZ )
        return EINVAL;

    if( CHEWED + tlen > total )
        return EINVAL;

    //tlen++; // strlcpy needs place for zero
    int tcopy = (tlen+1 > o_tlen) ? o_tlen : tlen+1;
    strlcpy( topic, pkt, tcopy );

    /*
    pkt += tlen;

    size_t vlen = total - CHEWED;
    if( vlen > MAX_SZ )
        return EINVAL;
    */

    return 0;
}

#endif

// --------------------------------------------------------------
// General reception code
// --------------------------------------------------------------

// TODO need global constant def!
//#define BUFLEN 1400
#define BUFLEN PKT_BUF_SIZE


// Wait for one incoming packet, parse and call corresponding callback

int mqtt_udp_recv( int fd, process_pkt callback )
{
    unsigned char buf[BUFLEN];
    int rc, src_ip;

    memset(buf, 0, sizeof(buf));
    rc = mqtt_udp_recv_pkt( fd, buf, BUFLEN, &src_ip );
    if(rc)
    {
        perror("pkt recv");
        return rc;
    }

    rc = mqtt_udp_parse_any_pkt( buf, BUFLEN, src_ip, callback );
    //if(rc) printf("err %d mqtt_udp_parse_any_pkt\n", rc );
    return rc;
}


// Process all incoming packets. Return only if error.

int mqtt_udp_recv_loop( process_pkt h )
{
    int fd, rc;

    fd = mqtt_udp_socket();
    if(fd < 0) return -1;

    rc = mqtt_udp_bind( fd );
    if(rc)
    {
        close(fd);
        return -2;
    }

    while(1)
    {
        rc = mqtt_udp_recv( fd, h );
        if(rc)
        {
            close(fd);
            return rc;
        }
    }

    close(fd);
    return 0;
}



// --------------------------------------------------------------
//
// Default packet processing
//
// - Reply to ping
// - TODO Reply to SUBSCRIBE
// - TODO Reply with PUBACK for PUBLISH with QoS
//
// TODO error handling
//
// --------------------------------------------------------------



void mqtt_udp_recv_reply( struct mqtt_udp_pkt *pkt )
{
    int fd = mqtt_udp_get_send_fd();

    switch( pkt->ptype )
    {
    case PTYPE_PINGREQ:
        // TODO err check
        if( fd > 0 ) mqtt_udp_send_ping_responce( fd, pkt->from_ip );
        break;
    //case PTYPE_SUBSCRIBE:
    //case PTYPE_PUBLISH:
    default:
        break;
    }
}



