/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Dump all incoming packets
 *
**/

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <locale.h>
#include <fcntl.h>
#include <errno.h>

#include "mqtt_udp.h"



#if MQTT_UDP_NEW_PARSER

int main(int argc, char *argv[])
{
    int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );
    if( rc ) {
        printf("mqtt_udp_recv_loop() = %d", rc);
        perror("error");
        exit(1);
    }

    return 0;
}


#else // MQTT_UDP_NEW_PARSER


static int handle_publish( int src_ip, int ptype, char *topic, char *value )
{
    if( ptype == PTYPE_PUBLISH )
        printf("PUBLISH '%s' = '%s'\n", topic, value );
    else
        printf("pkt type %d is '%s' = '%s'\n", ptype, topic, value );

    return 0;
}

// Handle incoming packet with no payload - PINGREQ, PINGRESP
static int handle_empty( int src_ip, int ptype )
{
    switch(ptype)
    {
    case PTYPE_PINGREQ:
        printf("PINGREQ\n");
        break;

    case PTYPE_PINGRESP:
        printf("PINGRESP\n");
        break;

    default:
        printf("pkt type %d?\n", ptype );
        break;

    }

    return 0;
}


static int handle_unknown( int src_ip, char *buf, int len )
{
    for(int i = 0; i < len; i++)
    {
        printf("0x%x ", buf[i]);
    }

    printf("\n");

    for(int i = 0; i < len; i++)
    {
        printf("%c", ((buf[i] > ' ') && (buf[i] < 0x7F)) ? buf[i] : '.'   );
    }
    printf("\n");

    return 0;
}


struct mqtt_udp_handlers h = {
    .handle_u = handle_unknown,
    .handle_e = handle_empty,
    .handle_p = handle_publish
};


int main(int argc, char *argv[])
{
    int rc = mqtt_udp_recv_loop( &h );
    if( rc ) {
        printf("mqtt_udp_recv_loop() = %d", rc);
        perror("error");
        exit(1);
    }

    return 0;
}

#endif // MQTT_UDP_NEW_PARSER

