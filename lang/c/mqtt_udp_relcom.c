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
static int delete_pkt( int in_pkt_id, int in_qos );
static void resend_pkts( void );

ARCH_MUTEX_TYPE relcom_mutex;

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
        delete_pkt( pkt->reply_to, MQTT_UDP_FLAGS_GET_QOS(pkt->pflags) );
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
    ARCH_MUTEX_INIT(relcom_mutex);
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


// Naive fixed size array impl
#define MAX_OUTGOING_PKT 30

#define MIN_LOW_QOS_ACK  2

#define MAX_RESEND_COUNT 3

static struct mqtt_udp_pkt *outgoing[MAX_OUTGOING_PKT];

static int insert_pkt( struct mqtt_udp_pkt *pp )
{
    ARCH_MUTEX_LOCK(relcom_mutex);

    int i;
    for( i = 0; i < MAX_OUTGOING_PKT; i++ )
    {
        if( outgoing[i] == 0 )
        {
            outgoing[i] = pp;
            ARCH_MUTEX_UNLOCK(relcom_mutex);
            return 0;
        }
    }

    ARCH_MUTEX_UNLOCK(relcom_mutex);

    return mqtt_udp_global_error_handler( MQ_Err_Memory, -1, "out of outgoing slots", "insert_pkts" );
}

static int delete_pkt( int in_pkt_id, int in_qos )
{
    ARCH_MUTEX_LOCK(relcom_mutex);

    int i;
    for( i = 0; i < MAX_OUTGOING_PKT; i++ )
    {
        if( outgoing[i] == 0 )
            continue;

        if( outgoing[i]->pkt_id != in_pkt_id )
            continue;

        printf( "found %d, ", in_pkt_id );

        if( MQTT_UDP_FLAGS_GET_QOS(outgoing[i]->pflags) == in_qos )
        {
            printf("same QoS %d, kill\n", in_qos );
            outgoing[i] = 0;
            ARCH_MUTEX_UNLOCK(relcom_mutex);
            return 0;
        }

        if( MQTT_UDP_FLAGS_GET_QOS(outgoing[i]->pflags) == in_qos+1 )
        {
            printf("-1 QoS %d, count\n", in_qos );
            outgoing[i]->ack_count++;

            if( outgoing[i]->ack_count >= MIN_LOW_QOS_ACK )
            {
            printf("enough acks, kill\n" );
            outgoing[i] = 0;
            }

        ARCH_MUTEX_UNLOCK(relcom_mutex);
        return 0;
        }

    }

    ARCH_MUTEX_UNLOCK(relcom_mutex);
    printf( "not found %d\n", in_pkt_id );
    return 0; // actualy is possible and ok
}

static void resend_pkts( void )
{
    ARCH_MUTEX_LOCK(relcom_mutex);

    int i;
    for( i = 0; i < MAX_OUTGOING_PKT; i++ )
    {
        if( outgoing[i] == 0 )
            continue;

        printf("resend %d\n", outgoing[i]->pkt_id );
        int rc  = build_and_send( outgoing[i] );
        if( rc )
            mqtt_udp_global_error_handler( MQ_Err_IO, rc, "resend error", "resend_pkts" );

        outgoing[i]->resend_count++;
        if( outgoing[i]->resend_count >= MAX_RESEND_COUNT )
        {
            printf("too many resends for %d, kill\n", outgoing[i]->pkt_id );
            outgoing[i] = 0;
        }
    }

    ARCH_MUTEX_UNLOCK(relcom_mutex);
}




