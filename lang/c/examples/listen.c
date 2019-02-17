/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Example program: Dump all incoming packets
 *
**/

#include "config.h"

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <locale.h>
#include <fcntl.h>
#include <errno.h>

#include "../mqtt_udp.h"


void usage(void)
{
    printf("mqtt_udp_listen [-s SignaturePassword]\n");
    exit(33);
}

int main(int argc, char *argv[])
{
    printf("Will listen to MQTT/UDP traffic and dump all the messages pass through\n\n");
    if( (argc == 2) || (argc > 3) )
        usage();

    if( argc == 3 )
    {
        if( strcmp( argv[1], "-s" ) )
            usage();

        const char *key = argv[2];
        mqtt_udp_enable_signature( key, strnlen(key,PKT_BUF_SIZE) ); // here PKT_BUF_SIZE == too big 
    }

#if 0
    const char *key = "signPassword";
    mqtt_udp_enable_signature( key, strlen(key) );
#endif

    while(1)
    {
        int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );
        if( rc ) {
            mqtt_udp_global_error_handler( MQ_Err_Other, rc, "recv_loop error", 0 );
        }
    }

    return 0;
}


