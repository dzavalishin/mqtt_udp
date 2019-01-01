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



#define BUFLEN PKT_BUF_SIZE // 64

int main(int argc, char *argv[])
{
    printf("Will listen to MQTT/UDP traffic and dump one next message\n");
    printf("Usage: -f to print all traffic (^C to stop)\n\n");

    int loop = 0;

    if( argc == 2 && (0==strcmp(argv[1], "-f")) )
        loop++;

    int fd, rc;
    unsigned char buf[BUFLEN];

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
#if 1
        int rc = mqtt_udp_recv( fd, mqtt_udp_dump_any_pkt );
        if( rc )
        {
            printf("mqtt_udp_recv err = %d\n", rc );
        }
#else
        memset(buf, 0, sizeof(buf));
        rc = mqtt_udp_recv_pkt( fd, buf, BUFLEN, 0 );

        char topic[BUFLEN];
        char value[BUFLEN];

        rc = mqtt_udp_parse_pkt( buf, BUFLEN, topic, BUFLEN, value, BUFLEN );
        if( rc )
        {
            printf("not parsed\n");
            mqtt_udp_dump( buf, BUFLEN );
        }
        else
            printf("'%s' = '%s'\n", topic, value );
#endif

    } while(loop);

    return 0;
}


