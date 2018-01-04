/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Main header file
 *
**/

#ifndef MQTT_UDP_H
#define MQTT_UDP_H

#include <stdlib.h>
#include "mqtt_udp_defs.h"

#ifdef __cplusplus
extern "C" {
#endif

#define MQTT_UDP_NEW_PARSER 1

// --------------------------------------------------------------------------
// Prepare socket
// --------------------------------------------------------------------------

int mqtt_udp_socket(void);
int mqtt_udp_bind( int fd ); // prepare to receive data

// --------------------------------------------------------------------------
// Send
// --------------------------------------------------------------------------

// Send PUBLISH packet - obsolete entry point, do not use
int mqtt_udp_send( int fd, char *topic, char *data );

// Send PUBLISH packet - obsolete entry point, do not use
int mqtt_udp_send_publish( int fd, char *topic, char *data );

int mqtt_udp_send_ping_request( int fd );
int mqtt_udp_send_ping_responce( int fd, int ip_addr );


// --------------------------------------------------------------------------
// Parse packet
// --------------------------------------------------------------------------

struct mqtt_udp_pkt
{
    int         from_ip;

    int         ptype;          // upper 4 bits
    int         pflags;         // lower 4 bits

    size_t      total;   	// length of the rest of pkt down from here

    int 	pkt_id;

    size_t      topic_len;
    char *      topic;

    size_t      value_len;
    char *      value;
};


typedef int (*process_pkt)( struct mqtt_udp_pkt *pkt );


int mqtt_udp_parse_any_pkt( const char *pkt, size_t plen, int from_ip, process_pkt callback );
int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o );


// --------------------------------------------------------------------------
// Recieve (unfinshed)
// --------------------------------------------------------------------------

#if MQTT_UDP_NEW_PARSER

// Wait for one incoming packet, parse and call corresponding callback
int mqtt_udp_recv( int fd, process_pkt callback );

// Process all incoming packets. Return only if error.
int mqtt_udp_recv_loop( process_pkt callback );


#else // MQTT_UDP_NEW_PARSER

//void (*mqtt_udp_handle_recv)( char *topic, char *data );

// Handle incoming PUBLISH type packet
typedef int (*mqtt_udp_handle_publish)( int src_ip, int ptype, char *topic, char *value );

// Handle incoming packet with no payload - PINGREQ, PINGRESP
typedef int (*mqtt_udp_handle_empty)( int src_ip, int ptype );

// Handle unknown incoming packets - raw content
typedef int (*mqtt_udp_handle_unknown)( int src_ip, char *data, int len );

struct mqtt_udp_handlers
{
    mqtt_udp_handle_publish	handle_p;
    mqtt_udp_handle_empty	handle_e;

    mqtt_udp_handle_unknown     handle_u;
};

// Wait for one incoming packet, parse and call corresponding callback
int mqtt_udp_recv( int fd, struct mqtt_udp_handlers *h );

// Process all incoming packets. Return only if error.
int mqtt_udp_recv_loop( struct mqtt_udp_handlers *h );

#endif // MQTT_UDP_NEW_PARSER


// --------------------------------------------------------------------------
// Was in local.h, use with caution
// --------------------------------------------------------------------------

// Broadcast packet
int mqtt_udp_send_pkt( int fd, char *data, size_t len );
// send packet to address
int mqtt_udp_send_pkt_addr( int fd, char *data, size_t len, int ip_addr );


// Low level packet recv
int mqtt_udp_recv_pkt( int fd, unsigned char *buf, size_t buflen, int *src_ip_addr );
// Parse PUBLISH
int mqtt_udp_parse_pkt( const char *pkt, size_t plen, char *topic, size_t o_tlen, char *value, size_t o_vlen );


#ifdef __cplusplus
}
#endif

#endif // MQTT_UDP_H
