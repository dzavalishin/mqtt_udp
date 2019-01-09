/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * General network code - must be different per OS
 *
**/

// TODO MINGW http://mingw.5.n7.nabble.com/Link-error-undefined-reference-to-htonl-4-with-MinGW-td502.html
// TODO MINGW add -lws2_32 and WSAStartup()
/*
    WSADATA wsa;

    err = WSAStartup(MAKEWORD(2,2),&wsa);
*/

#include "config.h"

#ifdef HAVE_SOCKET
#  include <sys/socket.h>
#endif

#ifdef HAVE_NETINET_IN_H
#  include <netinet/in.h>
#endif

#ifdef HAVE_ARPA_INET_H
#  include <arpa/inet.h>
#endif

#ifdef __MINGW32__
//#  include "winsock.h"
#  include "ws2tcpip.h"
#endif


#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <locale.h>
#include <fcntl.h>
#include <errno.h>

#include "mqtt_udp.h"


static int last_socket = -1;

int mqtt_udp_socket(void)
{
    int fd;

    if ((fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0)
    {
        perror("socket");
        //exit(1);
        return -1;
    }

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

    //last_socket = fd;
    return fd;
}


int mqtt_udp_bind( int fd )
{
    struct sockaddr_in srcaddr;

    memset(&srcaddr, 0, sizeof(srcaddr));

    srcaddr.sin_family = AF_INET;
#ifdef __MINGW32__
    srcaddr.sin_addr.s_addr = htonl(INADDR_ANY);
#else
    srcaddr.sin_addr.s_addr = htonl(INADDR_ANY);
#endif
    srcaddr.sin_port = htons(MQTT_PORT);

    return bind(fd, (struct sockaddr *) &srcaddr, sizeof(srcaddr));
/*
    if (bind(fd, (struct sockaddr *) &srcaddr, sizeof(srcaddr)) < 0) {
        perror("bind");
        exit(1);
    }
*/
}

// TODO move to separate source file to let user replace other funcs
int mqtt_udp_get_send_fd( void ) // TODO hack, get fd to send datagrams
{
    if( last_socket < 0 )
        last_socket = mqtt_udp_socket(); // TODO not thread safe
    return last_socket;
}


int mqtt_udp_close_fd( int fd ) 
{
    return close( fd );
}



