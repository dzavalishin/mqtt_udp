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


// --------------------------------------------------------------------------
//
// General packet representation
//
// --------------------------------------------------------------------------

struct mqtt_udp_pkt
{
    int         from_ip;

    int         ptype;          // upper 4 bits, not shifted
    int         pflags;         // lower 4 bits

    size_t      total;   	// length of the rest of pkt down from here

    int         pkt_id;

    size_t      topic_len;
    char *      topic;

    size_t      value_len;
    char *      value;
};


typedef int (*process_pkt)( struct mqtt_udp_pkt *pkt );





// --------------------------------------------------------------------------
// Send
// --------------------------------------------------------------------------


// Send PUBLISH packet
int mqtt_udp_send_publish( char *topic, char *data );

int mqtt_udp_send_subscribe( char *topic );


int mqtt_udp_send_ping_request( void );
//int mqtt_udp_send_ping_responce( int fd, int ip_addr );
int mqtt_udp_send_ping_responce( void );


// --------------------------------------------------------------------------
//
// Receive
//
// --------------------------------------------------------------------------


// Wait for one incoming packet, parse and call corresponding callback
int mqtt_udp_recv( int fd, process_pkt callback );

// Process all incoming packets. Return only if error.
int mqtt_udp_recv_loop( process_pkt callback );



// --------------------------------------------------------------------------
// util
// --------------------------------------------------------------------------

// does check topic name against wildcarded string, return 1 on match
int mqtt_udp_match( char *filter, char *topicName );

void mqtt_udp_dump( const char *buf, size_t len );

int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o );




// --------------------------------------------------------------------------
//
// Was in local.h, NOT TO BE USED outside of lib code
//
// --------------------------------------------------------------------------

int mqtt_udp_socket(void);
int mqtt_udp_bind( int fd ); // prepare to receive data

// Broadcast packet
int mqtt_udp_send_pkt( int fd, char *data, size_t len );

// send packet to address
int mqtt_udp_send_pkt_addr( int fd, char *data, size_t len, int ip_addr );

// Low level packet recv
int mqtt_udp_recv_pkt( int fd, char *buf, size_t buflen, int *src_ip_addr );

int mqtt_udp_get_send_fd( void ); // TODO hack, get fd to send datagrams

int mqtt_udp_close_fd( int fd );

// --------------------------------------------------------------------------
//
// Defauli packet processor, called before user callback and replies according
// to protocol requirements. You can replace it if you know what you do.
//
// NOT TO BE USED outside of lib code
//
// --------------------------------------------------------------------------

void mqtt_udp_recv_reply( struct mqtt_udp_pkt *pkt );

// --------------------------------------------------------------------------
//
// General packet representation - build to binary / parse from binary / etc
//
// NOT TO BE USED outside of lib code
//
// --------------------------------------------------------------------------

void mqtt_udp_clear_pkt( struct mqtt_udp_pkt *p );
int mqtt_udp_build_any_pkt( char *buf, size_t blen, struct mqtt_udp_pkt *p, size_t *out_len );
int mqtt_udp_parse_any_pkt( const char *pkt, size_t plen, int from_ip, process_pkt callback );


// --------------------------------------------------------------------------
//
// Packet flags
//
// --------------------------------------------------------------------------


#define MQTT_UDP_FLAGS_HAS_RETAIN(pflags)  ((pflags) & 0x1)
#define MQTT_UDP_FLAGS_HAS_QOS1(pflags)  ((pflags) & 0x2)
#define MQTT_UDP_FLAGS_HAS_QOS2(pflags)  ((pflags) & 0x4)
#define MQTT_UDP_FLAGS_HAS_DUP(pflags)  ((pflags) & 0x8)

// Flags field has bits which tell us to use packet id field
#define MQTT_UDP_FLAGS_HAS_ID(pflags)  ((pflags) & 0x6)


#define MQTT_UDP_FLAGS_SET_RETAIN(pflags)  ((pflags) |= 0x1)
#define MQTT_UDP_FLAGS_SET_QOS1(pflags)  ((pflags) |= 0x2)
#define MQTT_UDP_FLAGS_SET_QOS2(pflags)  ((pflags) |= 0x4)
#define MQTT_UDP_FLAGS_SET_DUP(pflags)  ((pflags) |= 0x8)




#ifdef __cplusplus
}
#endif

#endif // MQTT_UDP_H
