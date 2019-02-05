/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Main header file
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

/// MQTT/UDP packet.
struct mqtt_udp_pkt
{
    uint32_t    from_ip;        ///< Sender IP address

    int         ptype;          ///< packet type. Upper 4 bits, not shifted.
    int         pflags;         ///< Packet flags (QoS, etc). Lower 4 bits.

    size_t      total;          ///< Length of the rest of pkt down from here.

    uint32_t    pkt_id;         ///< PAcket ID, not currently used. Will be supported by TTRs.

    size_t      topic_len;      ///< Length of topic string, bytes.
    char *      topic;          ///< Topic string, 0-terminated.

    size_t      value_len;      ///< Length of value string, bytes.
    char *      value;          ///< Value string, 0-terminated.
};

/**
 * @brief Pointer to callback function that processes packet
 * 
 * Function must **NOT** assume that packet or its data exist
 * after callback return.
 * 
 * @param pkt Packet to process
 * 
 * @return 0 if ok, or error code
**/
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

/// Type of error
typedef enum {
    MQ_Err_Other,
    MQ_Err_Memory,      ///< ENOMEM
    MQ_Err_Establish,   ///< Unable to open socket or bind
    MQ_Err_IO,          ///< Net io
    MQ_Err_Proto,       ///< Broken packet
    MQ_Err_Timeout,
    MQ_Err_Invalid,     ///< Invalid parameter value.
} mqtt_udp_err_t;

/**
 * 
 * User error handler callback function.
 * 
 * @param type    Type of error
 * @param err_no  Error code
 * @param msg     Error message
 * @param arg     Error message parameter (additional info)
 * 
 * @return Must return ```err_no``` or 0 if we want caller to ignore error, if possible.
 * 
**/
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

/**
 * Kinds of configuration items.
 * 
 * Used by host-faced code for internal processing.
 * Does not affect network communications.
**/
typedef enum
{
    MQ_CFG_KIND_OTHER,
    MQ_CFG_KIND_TOPIC,  ///< Topics device works with, R/W
    MQ_CFG_KIND_INFO,   ///< Read-Only
    MQ_CFG_KIND_NODE,   ///< Node info, R/W
    //MQ_CFG_,
} mqtt_udp_rconfig_inetm_kind_t;


/// Value of configuration parameter
typedef union
{
    int32_t     b; ///< Boolean value.
    char *      s; ///< 0-termiated string value. Will reallocate, must be malloc'ed.
    int32_t     i; ///< Integer value.
    void *      o; ///< Other data.
} mqtt_udp_rconfig_item_value_t;

/// Definition of configuration parameter
typedef struct
{
    mqtt_udp_rconfig_item_type_t        type;   ///< Item (.value field) data type (string, bool, number, other)
    mqtt_udp_rconfig_inetm_kind_t       kind;   ///< Item kind, not processed by network code
    const char *                        name;   ///< Human readable name for this config parameter
    const char *                        topic;  ///< MQTT/UDP topic name for this config parameter
    mqtt_udp_rconfig_item_value_t       value;  ///< Current value
    mqtt_udp_rconfig_item_value_t       opaque; ///< user data item, not processed by MQTT/UDP code at all
} mqtt_udp_rconfig_item_t;


/// User function called by rconfig to load and save configuration items from/to local storage
typedef int (*mqtt_udp_rconfig_rw_callback)( int pos, int write );


int mqtt_udp_rconfig_client_init(char *mac_address_string, mqtt_udp_rconfig_rw_callback cb, mqtt_udp_rconfig_item_t *rconfig_items, int n_items );
int mqtt_udp_rconfig_set_string( int pos, char *string );

// Helpers for user to work with rconfig_items array

const char * rconfig_get_string_by_item_index( int pos, mqtt_udp_rconfig_inetm_kind_t kind );
int rconfig_find_by_string_value( const char *search, mqtt_udp_rconfig_inetm_kind_t kind );


// ==========================================================================
// --------------------------------------------------------------------------
//
//       NB! All the stuff below NOT TO BE USED outside of lib code
//
// --------------------------------------------------------------------------
// ==========================================================================




/**
 * @brief Architecture dependent: Create socket
 * 
 * Must be defined in ```glue/{dir}/udp_open.c``` for each target OS.
 * 
 * @return Socket descriptor that will be passed to UDP send/recv functions. Not used in any way else.
 * 
**/
int mqtt_udp_socket(void);

/**
 * @brief Architecture dependent: Prepare socket for reception on ```MQTT_PORT```
 * 
 * Must be defined in ```glue/{dir}/udp_open.c``` for each target OS.
 * 
 * @param fd Descriptor from ```mqtt_udp_socket()```
 * @return 0 if ok or error code.
 * 
**/
int mqtt_udp_bind( int fd );

/**
 * @brief Architecture dependent: Broadcast packet
 * 
 * Must be defined in ```glue/{dir}/udp_send_pkt.c``` for each target OS.
 * 
 * @param fd    Descriptor from ```mqtt_udp_socket()```
 * @param data  Packet data to send
 * @param len   Size of ```data```
 * 
 * @return 0 if ok or error code.
 * 
**/
int mqtt_udp_send_pkt( int fd, char *data, size_t len );

/**
 * @brief Architecture dependent: Send packet to address
 * 
 * Must be defined in ```glue/{dir}/udp_send_pkt.c``` for each target OS.
 * 
 * @param fd       Descriptor from ```mqtt_udp_socket()```
 * @param data     Packet data to send
 * @param len      Size of ```data```
 * @param ip_addr  IP address to send to
 * 
 * @return 0 if ok or error code.
 * 
**/
int mqtt_udp_send_pkt_addr( int fd, char *data, size_t len, uint32_t ip_addr );

/**
 * @brief Architecture dependent: Receive (wait for) packet
 * 
 * Must be defined in ```glue/{dir}/udp_recv_pkt.c``` for each target OS.
 * 
 * @param fd           Descriptor from ```mqtt_udp_socket()```
 * @param buf          Buffer to receive into
 * @param buflen       Size of ```buf```
 * @param src_ip_addr  Pointer to variable to put IP address of sender into (return)
 * 
 * @return Size of packet received
 * 
**/
int mqtt_udp_recv_pkt( int fd, char *buf, size_t buflen, uint32_t *src_ip_addr );

/**
 * @brief Architecture dependent: Destroy socket
 * 
 * Must be defined in ```glue/{dir}/udp_open.c``` for each target OS.
 * 
 * @param fd       Descriptor of sockey (from ```mqtt_udp_socket()```) to destroy.
 * 
 * @return 0 if ok or error
 * 
**/
int mqtt_udp_close_fd( int fd );

/// Internal, get reused socket for send
int mqtt_udp_get_send_fd( void ); // TODO hack, get fd to send datagrams

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

/**
 * @brief Architecture dependent: Get current time in milliseconds.
 * 
 * Actually it is **glue**, not **arch** dependent. Must be defined
 * in ```glue/{dir}/glue_time.c``` for each target OS.
 * 
 * Used in counting time between outgoing packets. 
 * 
 * @todo Rename 
 * 
 * @return Time in milliseconds. Base value is irrelevant.
 * 
**/
uint64_t mqtt_udp_arch_get_time_msec( void );

/**
 * @brief Architecture dependent: Sleep for given number of milliseconds.
 * 
 * Actually it is **glue**, not **arch** dependent. Must be defined
 * in ```glue/{dir}/glue_time.c``` for each target OS.
 * 
 * Used to reduce send speed. 
 * 
 * @todo Rename 
 * 
 * @param msec Time in milliseconds to sleep for. 
 * 
**/
void  mqtt_udp_arch_sleep_msec( uint32_t msec );


// --------------------------------------------------------------------------
//
// Packet flags
//
// --------------------------------------------------------------------------


#define MQTT_UDP_FLAGS_HAS_RETAIN(pflags)  ((pflags) & 0x1)   ///< Check RETAIN flag
#define MQTT_UDP_FLAGS_HAS_QOS1(pflags)  ((pflags) & 0x2)     ///< Check QoS 1 flag
#define MQTT_UDP_FLAGS_HAS_QOS2(pflags)  ((pflags) & 0x4)     ///< Check QoS 2 flag
#define MQTT_UDP_FLAGS_HAS_DUP(pflags)  ((pflags) & 0x8)      ///< Check DUP flag

// NB! MQTT/UDP does not use variable header == ID field
//
// Flags field has bits which tell us to use packet id field
//#define MQTT_UDP_FLAGS_HAS_ID(pflags)  ((pflags) & 0x6)


#define MQTT_UDP_FLAGS_SET_RETAIN(pflags)  ((pflags) |= 0x1)  ///< Set RETAIN flag
#define MQTT_UDP_FLAGS_SET_QOS1(pflags)  ((pflags) |= 0x2)    ///< Set QoS 1 flag
#define MQTT_UDP_FLAGS_SET_QOS2(pflags)  ((pflags) |= 0x4)    ///< Set QoS 2 flag
#define MQTT_UDP_FLAGS_SET_DUP(pflags)  ((pflags) |= 0x8)     ///< Set DUP flag




#ifdef __cplusplus
}
#endif

#endif // MQTT_UDP_H
