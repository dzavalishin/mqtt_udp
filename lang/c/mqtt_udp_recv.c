/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 * General reception code
 *
 * - listen loop
 * - protocol replies
 *
**/

#include "config.h"

#include <string.h>
// Just for perror(), remove it and use user error callback!
//#include <stdio.h>

#include "mqtt_udp.h"





// --------------------------------------------------------------
//
// Wait for one incoming packet, parse and call corresponding
// callback
//
// --------------------------------------------------------------


int mqtt_udp_recv( int fd, process_pkt callback )
{
    char buf[PKT_BUF_SIZE];
    int rc;
    uint32_t src_ip;

    memset(buf, 0, sizeof(buf));
    rc = mqtt_udp_recv_pkt( fd, buf, PKT_BUF_SIZE, &src_ip );
    if(rc < 0)
    {
        //perror("pkt recv");
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



// --------------------------------------------------------------
//
// Default packet processing, called from mqtt_udp_parse_any_pkt()
//
// - Reply to ping
// - TODO Reply to SUBSCRIBE
// - TODO Reply with PUBACK for PUBLISH with QoS
//
// TODO error handling
//
// --------------------------------------------------------------



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



