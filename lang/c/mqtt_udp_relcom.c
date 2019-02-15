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




