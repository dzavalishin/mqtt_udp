/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief General reception code
 *
 * * Main listen loop
 * * Automatic protocol replies
 *
**/

#include "config.h"

#include <string.h>

#include "mqtt_udp.h"




/**
 * 
 * @brief Wait for one incoming packet, parse and call corresponding callback
 * 
 * @deprecated
 * 
 * @param fd Socket
 * @param callback Incoming packets handler function.
 * 
**/
int mqtt_udp_recv( int fd, process_pkt callback )
{
    char buf[PKT_BUF_SIZE];
    int rc;
    uint32_t src_ip;

    memset(buf, 0, sizeof(buf));
    rc = mqtt_udp_recv_pkt( fd, buf, PKT_BUF_SIZE, &src_ip );
    if(rc < 0)
    {
        rc = mqtt_udp_global_error_handler( MQ_Err_IO, rc, "packet recv error", "" );
        return rc;
    }

    //rc = mqtt_udp_parse_any_pkt( buf, PKT_BUF_SIZE, src_ip, callback );
    rc = mqtt_udp_parse_any_pkt( buf, rc, src_ip, callback );
    //if(rc) printf("err %d mqtt_udp_parse_any_pkt\n", rc );
    return rc;
}


// --------------------------------------------------------------
//
// Process all incoming packets. Return only if error.
//
// --------------------------------------------------------------

/**
 * 
 * @brief Main receive loop.
 * 
 * Must be called by user thread, will receive packets 
 * and call user handler to process them. Other protocol
 * handlers such as automatic PING reply and remote
 * config client (if initialized) will be fed with packets
 * too.
 * 
 * If you need packets reception at all, this function must be ran.
 * 
 * @param h Incoming packets handler (callback) function.
 * 
 * @return Error code. It does not return until error.
 * 
 * @todo Break down into ```int mqtt_udp_init( void )``` and ``int mqtt_udp_recv_loop( process_pkt h )``?
 * 
**/
int mqtt_udp_recv_loop( process_pkt h )
{
    int fd, rc;

    fd = mqtt_udp_socket();
    if(fd < 0) return -1;

    rc = mqtt_udp_bind( fd );
    if(rc)
    {
        mqtt_udp_close_fd( fd );
        return -2;
    }

    while(1)
    {
        rc = mqtt_udp_recv( fd, h );
        if(rc)
        {
            mqtt_udp_close_fd( fd );
            return rc;
        }
    }

    mqtt_udp_close_fd( fd );
    return 0;
}




/** 
 * 
 * @brief Default packet processing, called from mqtt_udp_parse_any_pkt()
 * 
 * * Reply to ping
 * 
 * @todo Reply to SUBSCRIBE? Not sure.
 * @todo Reply with PUBACK for PUBLISH with QoS
 * @todo Error handling
 * 
**/ 



void mqtt_udp_recv_reply( struct mqtt_udp_pkt *pkt )
{
    //int fd = mqtt_udp_get_send_fd();

    switch( pkt->ptype )
    {
    case PTYPE_PINGREQ:
        // TODO err check
        //if( fd > 0 ) mqtt_udp_send_ping_responce( fd, pkt->from_ip );
        mqtt_udp_send_ping_responce();
        break;
    //case PTYPE_SUBSCRIBE:
    //case PTYPE_PUBLISH:
    default:
        break;
    }
}



