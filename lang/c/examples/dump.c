/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * 
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * @file
 * @brief Example program: Binary dump of all messages
 *
**/

#include "config.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>

#include "../mqtt_udp.h"




int main(int argc, char *argv[])
{
    printf("Will listen to MQTT/UDP traffic and hex dump one next message\n");
    printf("Usage: -f to print all traffic (^C to stop)\n\n");

    int loop = 0;

    if( argc == 2 && (0==strcmp(argv[1], "-f")) )
        loop++;

    int fd, rc;
    char buf[PKT_BUF_SIZE];

    fd = mqtt_udp_socket();

    if(fd < 0) {
        perror("socket");
        exit(1);
    }

    rc = mqtt_udp_bind( fd );
    if(rc) {
        perror("bind");
        exit(1);
    }

    do {
        int rc = mqtt_udp_recv_pkt( fd, buf, sizeof buf, 0 );

        if( rc < 0 )
            printf("mqtt_udp_recv err = %d\n", rc );
        else
            mqtt_udp_dump( buf, rc );        

    } while(loop);

    return 0;
}


