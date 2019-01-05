/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Data reception example code
 *
**/

//#include <sys/types.h>
//#include <sys/socket.h>
//#include <netinet/in.h>
//#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
//#include <locale.h>
//#include <fcntl.h>
#include <errno.h>

#include "mqtt_udp.h"



//#define BUFLEN PKT_BUF_SIZE // 64

int main(int argc, char *argv[])
{
    printf("Will listen to MQTT/UDP traffic and dump one next message\n");
    printf("Usage: -f to print all traffic (^C to stop)\n\n");

    int loop = 0;

    if( argc == 2 && (0==strcmp(argv[1], "-f")) )
        loop++;

    int fd, rc;
    unsigned char buf[PKT_BUF_SIZE];

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
        int rc = mqtt_udp_recv( fd, mqtt_udp_dump_any_pkt );
        if( rc )
        {
            printf("mqtt_udp_recv err = %d\n", rc );
        }

    } while(loop);

    return 0;
}


