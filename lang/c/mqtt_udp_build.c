/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * 
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * @file
 * @brief Generalized MQTT/UDP packet builder
 *
**/

#include "config.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#include "mqtt_udp.h"

static int pack_len( char *buf, size_t *blen, int *used, int data_len );

int encode_TTR( char **bp, size_t *blen, char type, char *data, int dlen );
int encode_int32_TTR( char **bp, size_t *blen, char type, uint32_t value );
int encode_int64_TTR( char **bp, size_t *blen, char type, uint64_t value );



// -----------------------------------------------------------------------
// Build
// -----------------------------------------------------------------------

static int32_t packet_number_generator;


/**
 * @brief Build outgoing binary packet representation.
 * 
 * @param buf      Buffer to put resulting packet to
 * @param blen     Size of buffer in bytes
 * @param p        Packet to encode
 * @param out_len  Resulting length of build packet in bytes
 * 
 * @return 0 on success or error code
 * 
**/
int mqtt_udp_build_any_pkt( char *buf, size_t blen, struct mqtt_udp_pkt *p, size_t *out_len )
{
    int rc;
    // TODO check for consistency - if pkt has to have topic & value and has it

    int tlen = p->topic ? p->topic_len : 0;
    int dlen = p->value ? p->value_len : 0;

    char *bp = buf;

    if( out_len ) *out_len = 0;

    *bp++ = (p->ptype & 0xF0) | (p->pflags & 0x0F);
    blen--;

    // MQTT payload size, not incl TTRs
    int total = tlen + dlen + 2;

    // Not supported in MQTT/UDP: if(MQTT_UDP_FLAGS_HAS_ID(p->pflags)) total += 2;

    int used = 0;
    rc = pack_len( bp, &blen, &used, total );
    if( rc ) return rc;
    bp += used;

    if( total > blen )
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );

    /* Not supported in MQTT/UDP
    if(MQTT_UDP_FLAGS_HAS_ID(p->pflags))
    {
        *bp++ = (p->pkt_id >> 8) & 0xFF;
        *bp++ = p->pkt_id & 0xFF;
        blen -= 2;
    }
    */

    if( tlen )
    {
        // Encode topic len
        *bp++ = (tlen >>8) & 0xFF;
        *bp++ = tlen & 0xFF;
        blen -= 2;

        const char *topic = p->topic;
        //NB! Must be UTF-8
        while( tlen-- > 0 )
        {
            if( blen == 0 ) return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );
            *bp++ = *topic++;
            blen--;
        }

        const char *data = p->value;
        while( dlen-- > 0 )
        {
            if( blen == 0 ) return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );
            *bp++ = *data++;
            blen--;
        }

    }

    if( p->pkt_id == 0 ) 
        p->pkt_id = packet_number_generator++;

    rc = encode_int32_TTR( &bp, &blen, 'n', p->pkt_id );
    if( rc ) return rc;

    // NB! This is a signature TTR, it must me the last one.

    // Signature TTR needs this many bytes
#define SIGNATURE_TTR_SIZE (MD5_DIGEST_SIZE+2)
    if(mqtt_udp_hmac_md5 != 0)
    {
        // Will sign
        if( blen < SIGNATURE_TTR_SIZE )
            return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "signature" );

        //unsigned char signature[MD5_DIGEST_SIZE];
        unsigned char *signature = (unsigned char *)bp+2;        
        mqtt_udp_hmac_md5( (unsigned char *)buf, bp-buf, signature );
        bp[0] = 's';
        bp[1] = ( 0x7F & MD5_DIGEST_SIZE );
        //bp[1] = MD5_DIGEST_SIZE;

        bp += SIGNATURE_TTR_SIZE;
        blen -= SIGNATURE_TTR_SIZE;
    }

    if( out_len ) *out_len = bp - buf;

    return 0;
}


// -----------------------------------------------------------------------
// TTRs
// -----------------------------------------------------------------------

int encode_TTR( char **bp, size_t *blen, char type, char *data, int dlen )
{
    if( *blen < 2 ) 
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );

    // TTR type byte
    *(*bp)++ = type; 

    // TTR content len
    int used = 0;
    int rc = pack_len( *bp, blen, &used, dlen ); 
    if( rc ) return rc;
    *bp += used;

    if( *blen < dlen ) 
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );

    //TTR content
    memcpy( *bp, data, dlen ); 
    *bp += dlen;

    return 0;
}

int encode_int32_TTR( char **bp, size_t *blen, char type, uint32_t value )
{
    const int bytes = 4; // 32 bits
    char out[bytes]; 
    int i;
	
    for( i = 0; i < bytes; i++ )
		out[i] = (char)(value >> (8*(bytes - i - 1)) );

    return encode_TTR( bp, blen, type, out, sizeof out );
}

int encode_int64_TTR( char **bp, size_t *blen, char type, uint64_t value )
{
    const int bytes = 8; // 64 bits
    char out[bytes]; 
    int i;
	
    for( i = 0; i < bytes; i++ )
		out[i] = (char)(value >> (8*(bytes - i - 1)) );

    return encode_TTR( bp, blen, type, out, sizeof out );
}





// -----------------------------------------------------------------------
// Bits
// -----------------------------------------------------------------------


/// Encode payload length.
static int pack_len( char *buf, size_t *blen, int *used, int data_len )
{
    *used = 0;
    while( 1 )
    {
        if( *blen == 0 ) return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );

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




