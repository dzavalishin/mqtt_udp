/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * General network code - must be different per OS
 *
**/



//#include "../../config.h"
#include "config.h"
#include "../../mqtt_udp.h"
#include "udp_io.h"




// MQTT/UDP library requests two sockets, one for recv, one for xmit.
// As we have no bind() we put both on MQTT_PORT, and second one takes
// over. If second one is send, it kills recption.
//
// That's why we keep it here and reuse for all calls.

static int last_fd = -1;


int mqtt_udp_socket(void)
{
#if DEBUG
    printf("MQTT/UDP mk socket \n");
#endif
    if( last_fd >= 0 ) return last_fd;

    UDPSOCKET *fd = NutUdpCreateSocket( MQTT_PORT );


    if( fd == 0 )
    {
        mqtt_udp_global_error_handler( MQ_Err_Establish, (int)fd, "socket creation error", "" );
        return 0;
    }

    /*
    {
        int broadcast=1;
        setsockopt(fd, SOL_SOCKET, SO_BROADCAST, &broadcast, sizeof(broadcast));
    }

    {
        int enable = 1;
        setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int));

        //if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < 0)
       	//    error("setsockopt(SO_REUSEADDR) failed");
    }
    */

    uint16_t sz = PKT_BUF_SIZE;
    NutUdpSetSockOpt( fd, SO_RCVBUF, &sz, sizeof(uint16_t) );

    last_fd = (int) fd;

    return (int) fd;
}


int mqtt_udp_bind( int fd )
{
#if DEBUG
    printf("MQTT/UDP bind \n");
#endif
    return 0;
}



int mqtt_udp_close_fd( int fd ) 
{
    return NutUdpDestroySocket( (UDPSOCKET *) fd );
}

