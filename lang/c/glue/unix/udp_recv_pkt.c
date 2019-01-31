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

#include "../../config.h"

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <locale.h>
#include <fcntl.h>
#include <errno.h>

#include "../../mqtt_udp.h"
#include "udp_io.h"


// TODO other OS bindings

int mqtt_udp_recv_pkt( int fd, char *buf, size_t buflen, uint32_t *src_ip_addr )
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

    int rc = recvfrom(fd, buf, buflen, 0, (struct sockaddr *) &addr, &slen);

    if( src_ip_addr ) *src_ip_addr = ntohl( addr.sin_addr.s_addr );

    return rc;
}

