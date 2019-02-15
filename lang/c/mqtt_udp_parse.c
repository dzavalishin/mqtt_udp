/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp

 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Generalized MQTT/UDP packet parser
 *
**/

#include "config.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#include "mqtt_udp.h"


static size_t mqtt_udp_decode_size( const char **pkt );
static size_t mqtt_udp_decode_topic_len( const char *pkt );

static void mqtt_udp_call_packet_listeners( struct mqtt_udp_pkt *pkt );

static int32_t TTR_decode_int32( const char *data );
static int TTR_check_signature( const char *pkt_start, size_t pkt_len, const char *signature );

// -----------------------------------------------------------------------
// parse
// -----------------------------------------------------------------------



//#define MQTT_UDP_PKT_HAS_ID(pkt)  ((pkt.pflags) & 0x6)


/// Sanity check size
#define MAX_SZ (4*1024)
/// How many bytes are processed already
#define CHEWED (pkt - pstart)


/**
 * 
 * @brief Parse incoming packet.
 * 
 * Call callback function with resulting packet. Note that packet
 * and it's contents will be deallocated after return from callback.
 * 
 * @param pkt       Incoming binary packet data from UDP packet.
 * @param plen      Incoming packet data length.
 * @param from_ip   Source IP address of packet.
 * @param callback  User function to call on packet parsed.
 * 
 * @return 0 on success or error code.
**/
int mqtt_udp_parse_any_pkt( const char *pkt, size_t plen, uint32_t from_ip, process_pkt callback )
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
    o.topic = o.value = 0;
    o.topic_len = o.value_len = 0;

    if( o.total+2 > plen )        
        return mqtt_udp_global_error_handler( MQ_Err_Proto, -2, "packet too short", "" );

    //const char *end_hdr = pkt; // end of header, start of payload
    const char *ttrs_start = pkt+o.total; // end of payload, start of TTRs

    // NB! MQTT/UDP does not use variable header (ID) field
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
    //o.topic_len = strnlen( o.topic, MAX_SZ );
    o.topic_len = strnlen( o.topic, tlen );


    pkt += tlen;

    size_t vlen = o.total - CHEWED + 2;
    if( vlen > MAX_SZ )
        return mqtt_udp_global_error_handler( MQ_Err_Proto, -5, "packet value len > pkt len", "" );

    // Packet with value?
    if( o.ptype != PTYPE_PUBLISH )
        goto parse_ttrs;

    //vlen++; // strlcpy needs place for zero
    o.value = malloc( vlen+2 );
    if( o.value == 0 )
    {
        free( o.topic );
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );
    }
    strlcpy( o.value, pkt, vlen+1 );
    //o.value_len = strnlen( o.value, MAX_SZ );
    o.value_len = strnlen( o.value, vlen );

parse_ttrs:
    ;
#if 1
    const char *ttrs = ttrs_start; // Current position in TTRs
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
        // Have TTR, process it
        switch(ttr_type)
        {
        case 'n': o.pkt_id   = TTR_decode_int32( ttrs ); break;
        case 'r': o.reply_to = TTR_decode_int32( ttrs ); break;
        case 's': 
            if( ttr_len <= 0 )
            {
                err = mqtt_udp_global_error_handler( MQ_Err_Proto, -6, "signature TTR len != 16", "" );
                if( err != 0 ) goto cleanup;
            }
            o.is_signed = TTR_check_signature( pstart, ttr_start - pstart, ttrs );
            break;
        default: break;
        }

        ttrs_len -= ttrs - ttr_start; // type & len fields
        ttrs_len -= ttr_len;          // TTR data

        ttrs += ttr_len;


        if( ttrs_len < 0 )
        {
            err = mqtt_udp_global_error_handler( MQ_Err_Proto, -6, "TTRs len < 0", "" );
            goto cleanup;
        }
    }
#endif

    mqtt_udp_recv_reply( &o );
    mqtt_udp_call_packet_listeners( &o );
    callback( &o );

cleanup:
    //if( o.topic ) free( o.topic );
    //if( o.value ) free( o.value );
    mqtt_udp_free_pkt( &o );

    return err;
}









/// List of callbacks to call for incoming packets.
struct listeners_list {
    struct listeners_list *next;    ///< Next list element or 0.
    process_pkt listener;           ///< Callback to call for incoming packet.
};

static struct listeners_list * listeners = 0;

/**
 * 
 * @brief Register one more listener to get incoming packets.
 * 
 * Used by mqtt_udp lib itself to connect subsystems.
 * 
 * @param listener Callback to call when packet arrives.
 * 
**/
void mqtt_udp_add_packet_listener( process_pkt listener )
{
    struct listeners_list * lp = malloc( sizeof( struct listeners_list ) );

    lp->next = listeners;
    lp->listener = listener;

    listeners = lp;
}

/// Pass packet to all listeners
static void mqtt_udp_call_packet_listeners( struct mqtt_udp_pkt *pkt )
{
    struct listeners_list * lp;
    for( lp = listeners ; lp ; lp = lp->next )
    {
        //int rc = 
        lp->listener( pkt );
        //if( rc ) break;
    }
}






// -----------------------------------------------------------------------
//
// decoders
//
// -----------------------------------------------------------------------



/// Decode payload size dynamic length int
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

/// Decode fixed 2-byte integer.
static size_t mqtt_udp_decode_topic_len( const char *pkt )
{
    return (pkt[0] << 8) | pkt[1];
}


static int32_t TTR_decode_int32( const char *data )
{
    uint32_t v;

    v  = (data[0] << 24); 
    v |= (data[1] << 16); 
    v |= (data[2] << 8);
    v |=  data[3];

    return v;
}

static int TTR_check_signature( const char *pkt_start, size_t pkt_len, const char *in_signature )
{
    // Not set up, can't check
    if(mqtt_udp_hmac_md5 == 0)
        return 0;

    unsigned char us_signature[MD5_DIGEST_SIZE];
    mqtt_udp_hmac_md5( (unsigned char *)pkt_start, pkt_len, us_signature );

    int ok = ! memcmp( in_signature, us_signature, MD5_DIGEST_SIZE );
    if( !ok )
        mqtt_udp_global_error_handler( MQ_Err_Proto, -6, "Incorrect signature", "" );
    return ok;
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

/**
 * @brief Dump packet.
 * 
 * @param o Packet pointer.
 * 
 * @return Allways 0.
**/
int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o )
{
    const char *tn = ptname[ o->ptype >> 4 ];

    printf( "pkt %s flags %x, id %lx from %d.%d.%d.%d",
            tn, o->pflags, (long)o->pkt_id,
            (int)(0xFF & (o->from_ip >> 24)),
            (int)(0xFF & (o->from_ip >> 16)),
            (int)(0xFF & (o->from_ip >> 8)),
            (int)(0xFF & (o->from_ip))
          );

    if( o->topic_len > 0 )
        printf(" topic '%s'", o->topic );

    if( o->value_len > 0 )
        printf(" = '%s'", o->value );

    if( o->is_signed )
        printf(" SIGNED" );

    printf( "\n");
    return 0;
}



