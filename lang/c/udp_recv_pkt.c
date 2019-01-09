/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * UDP packet reception for POSIX, must be rewritten per OS
 *
**/

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


// TODO other OS bindings

int mqtt_udp_recv_pkt( int fd, char *buf, size_t buflen, int *src_ip_addr )
{
    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    //addr.sin_addr.s_addr = inet_addr(IP);
    //addr.sin_port = htons(MQTT_PORT);

    //int slen = sizeof(addr);
    socklen_t slen = sizeof(addr);

    //memset(buf, 0, sizeof(buf));
    memset( buf, 0, buflen );

    recvfrom(fd, buf, buflen, 0, (struct sockaddr *) &addr, &slen);

    if( src_ip_addr ) *src_ip_addr = ntohl( addr.sin_addr.s_addr );

    return 0;
}

