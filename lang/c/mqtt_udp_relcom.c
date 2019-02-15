/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp

 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Reliable communications layer
 *
**/

#include "config.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#include "mqtt_udp.h"


static int build_and_send( struct mqtt_udp_pkt *pp );

static int insert_pkt( struct mqtt_udp_pkt *pp );
static int delete_pkt( int pkt_id );
static void resend_pkts( void );

// -----------------------------------------------------------------------
//
// Interface
//
// -----------------------------------------------------------------------

/**
 * 
 * @brief Compose and send PUBLISH packet.
 * 
 * @param topic  Message topic
 * @param data   Message value, usually text string
 * 
 * @returns 0 if ok, or error code
**/
int mqtt_udp_send_publish_qos( char *topic, char *data, int qos )
{
    struct mqtt_udp_pkt *pp = malloc(sizeof( struct mqtt_udp_pkt ));
    int rc;

    if( 0 == pp )
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "send_publish_qos" );

    mqtt_udp_clear_pkt( pp );

    pp->ptype = PTYPE_PUBLISH;
    pp->topic = topic;
    pp->value = data;
    pp->topic_len = strnlen( topic, PKT_BUF_SIZE );
    pp->value_len = strnlen( data, PKT_BUF_SIZE );

    MQTT_UDP_FLAGS_SET_QOS(pp->pflags, qos);

    rc = insert_pkt( pp );
    if( rc )
    {
        // Still attempt to send!
        build_and_send( pp );

        free( pp );
        return rc;
    }

    return build_and_send( pp );
}


// -----------------------------------------------------------------------
//
// Listener
//
// -----------------------------------------------------------------------


static int relcom_listener( struct mqtt_udp_pkt *pkt )
{
    if( pkt->ptype == PTYPE_PUBACK )
    {
        printf("got ack to %d\n", pkt->reply_to );
        if( pkt->reply_to == 0 )
        {
            mqtt_udp_global_error_handler( MQ_Err_Proto, -1, "puback reply_to 0", "relcom_listener" );
            return 0;
        }
        delete_pkt( pkt->reply_to );
    }

    return 0;
}


// -----------------------------------------------------------------------
//
// Init
//
// -----------------------------------------------------------------------



void mqtt_udp_relcom_init(void)
{
    mqtt_udp_add_packet_listener( relcom_listener );
}


/**
 * 
 * @brief Must be called by application once in 100 msec.
 *
 * Does housekeeping: packets resend, cleanup.
 * 
**/
void mqtt_udp_relcom_housekeeping( void )
{
    resend_pkts();
}


// -----------------------------------------------------------------------
//
// Send
//
// -----------------------------------------------------------------------


static int build_and_send( struct mqtt_udp_pkt *pp )
{
    char buf[PKT_BUF_SIZE];
    size_t out_size;
    int rc;
    //mqtt_udp_dump_any_pkt( &p );

    rc = mqtt_udp_build_any_pkt( buf, PKT_BUF_SIZE, pp, &out_size );
    if( rc ) return rc;

    //mqtt_udp_dump( buf, out_size );

    return mqtt_udp_send_pkt( mqtt_udp_get_send_fd(), buf, out_size );
}

// -----------------------------------------------------------------------
//
// Outgoing list
//
// -----------------------------------------------------------------------


static int insert_pkt( struct mqtt_udp_pkt *pp );
static int delete_pkt( int pkt_id );
static void resend_pkts( void );




