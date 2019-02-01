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
#include <stdint.h>
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
    uint32_t    from_ip;        // Sender IP address

    int         ptype;          // Upper 4 bits, not shifted
    int         pflags;         // Lower 4 bits

    size_t      total;   	// Length of the rest of pkt down from here

    uint32_t    pkt_id;

    size_t      topic_len;
    char *      topic;

    size_t      value_len;
    char *      value;
};


typedef int (*process_pkt)( struct mqtt_udp_pkt *pkt );





// --------------------------------------------------------------------------
//
// Send
//
// --------------------------------------------------------------------------


// Send PUBLISH packet
int mqtt_udp_send_publish( char *topic, char *data );

int mqtt_udp_send_subscribe( char *topic );


int mqtt_udp_send_ping_request( void );

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
//
// Control
//
// --------------------------------------------------------------------------

// Set min time between packets sent, msec
void mqtt_udp_set_throttle(int msec);



// --------------------------------------------------------------------------
//
// Util
//
// --------------------------------------------------------------------------

// does check topic name against wildcarded string, return 1 on match
int mqtt_udp_match( char *filter, char *topicName );

void mqtt_udp_dump( const char *buf, size_t len );

int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o );



// --------------------------------------------------------------------------
//
// Error handling
//
// --------------------------------------------------------------------------

typedef enum {
    MQ_Err_Other,
    MQ_Err_Memory,      // ENOMEM
    MQ_Err_Establish,   // open socket
    MQ_Err_IO,          // net io
    MQ_Err_Proto,       // broken pkt
    MQ_Err_Timeout,
} mqtt_udp_err_t;

typedef int err_func_t( mqtt_udp_err_t type, int err_no , char * msg, char * arg );

void mqtt_udp_set_error_handler( err_func_t *handler );



// --------------------------------------------------------------------------
//
// Remote config
//
// Passive remote config - device keeps configuration
// in local storage, external utility asks for a list
// of parameters to set up and sends new parameter values.
// Device writes them down and uses as settings.
//
// --------------------------------------------------------------------------


/// Types of configuration items
typedef enum
{
    MQ_CFG_TYPE_BOOL,
    MQ_CFG_TYPE_STRING,
    MQ_CFG_TYPE_INT32,
} mqtt_udp_rconfig_item_type_t;

/// Value of configuration parameter
typedef union
{
    int32_t     b;
    char *      s; // will reallocate, must be malloc'ed
    int32_t     i;
    void *      o; // other
} mqtt_udp_rconfig_item_value_t;

/// Definition of configuration parameter
typedef struct
{
    mqtt_udp_rconfig_item_type_t        type;
    const char *                        name;  ///< Human readable name for this config parameter
    const char *                        topic; ///< MQTT/UDP topic name for this config parameter
    mqtt_udp_rconfig_item_value_t       value; ///< Current or default value

} mqtt_udp_rconfig_item_t;


/// User function called by rconfig to load and save configuration items from/to local storage
typedef int (*mqtt_udp_rconfig_rw_callback)( int pos, int write );


int mqtt_udp_rconfig_client_init(char *mac_address_string, mqtt_udp_rconfig_rw_callback cb, mqtt_udp_rconfig_item_t *rconfig_items, int n_items );



// ==========================================================================
// --------------------------------------------------------------------------
//
//       NB! All the stuff below NOT TO BE USED outside of lib code
//
// --------------------------------------------------------------------------
// ==========================================================================




int mqtt_udp_socket(void);
int mqtt_udp_bind( int fd ); // prepare to receive data

// Broadcast packet
int mqtt_udp_send_pkt( int fd, char *data, size_t len );

// send packet to address
int mqtt_udp_send_pkt_addr( int fd, char *data, size_t len, uint32_t ip_addr );

// Low level packet recv
int mqtt_udp_recv_pkt( int fd, char *buf, size_t buflen, uint32_t *src_ip_addr );

int mqtt_udp_get_send_fd( void ); // TODO hack, get fd to send datagrams

int mqtt_udp_close_fd( int fd );

// Introduce pause if we send too frequently
void mqtt_udp_throttle( void );


// Additional subsystems use to snoop for incoming packets
// Listener returns 0 if it is ok to pass packet to next listeners, non-zero to consume.
void mqtt_udp_add_packet_listener( process_pkt listener );

// --------------------------------------------------------------------------
//
// Defauli packet processor, called before user callback and replies according
// to protocol requirements. 
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
void mqtt_udp_free_pkt( struct mqtt_udp_pkt *p );

int mqtt_udp_build_any_pkt( char *buf, size_t blen, struct mqtt_udp_pkt *p, size_t *out_len );
int mqtt_udp_parse_any_pkt( const char *pkt, size_t plen, uint32_t from_ip, process_pkt callback );

// --------------------------------------------------------------------------
//
// Error handling
//
// --------------------------------------------------------------------------


// Returns:
//	rc - caller must return and report error if possible
//      0  - caller must ignore and continue
//      does not return at all if user decided that error is fatal

int mqtt_udp_global_error_handler( mqtt_udp_err_t type, int err_no, char *msg, char *arg );

// --------------------------------------------------------------------------
//
// Machdep
//
// --------------------------------------------------------------------------


uint64_t mqtt_udp_arch_get_time_msec( void );
void  mqtt_udp_arch_sleep_msec( uint32_t msec );


// --------------------------------------------------------------------------
//
// Packet flags
//
// --------------------------------------------------------------------------


#define MQTT_UDP_FLAGS_HAS_RETAIN(pflags)  ((pflags) & 0x1)
#define MQTT_UDP_FLAGS_HAS_QOS1(pflags)  ((pflags) & 0x2)
#define MQTT_UDP_FLAGS_HAS_QOS2(pflags)  ((pflags) & 0x4)
#define MQTT_UDP_FLAGS_HAS_DUP(pflags)  ((pflags) & 0x8)

// NB! MQTT/UDP does not use variable header == ID field
//
// Flags field has bits which tell us to use packet id field
//#define MQTT_UDP_FLAGS_HAS_ID(pflags)  ((pflags) & 0x6)


#define MQTT_UDP_FLAGS_SET_RETAIN(pflags)  ((pflags) |= 0x1)
#define MQTT_UDP_FLAGS_SET_QOS1(pflags)  ((pflags) |= 0x2)
#define MQTT_UDP_FLAGS_SET_QOS2(pflags)  ((pflags) |= 0x4)
#define MQTT_UDP_FLAGS_SET_DUP(pflags)  ((pflags) |= 0x8)




#ifdef __cplusplus
}
#endif

#endif // MQTT_UDP_H
