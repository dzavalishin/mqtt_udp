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
 * @brief Part of global test: Wait for message with given topic/data. Timeout.
 *
 * This program is called by test/runner to check for other programs are sending messages.
 *
 *
**/

#include "config.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>

#include "../mqtt_udp.h"


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//
// NB! Used in regress tests! Do not modify!
//
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


char *topic;
char *msg;
int check_sig = 0;


int check_pkt(struct mqtt_udp_pkt *pkt)
{
    if( pkt->ptype != PTYPE_PUBLISH )
        return 0;

    if(
       (0 == strcmp( topic, pkt->topic ))
       &&
       (0 == strcmp( msg, pkt->value ))
      )
    {
        if(check_sig && (!pkt->is_signed))
        {
            printf("Got with NO correct signature!\n");
            exit(3);
        }
        printf("Got it!\n");
        exit(0);
    }

    return 0;
}


void usage( void )
{
    printf("Will listen to MQTT/UDP traffic and wait for message\n");
    printf("Usage: waitmsg [-s SignaturePassword] topic data\n\n");
    exit(33);
}


int main(int argc, char *argv[])
{
    time_t start;

    long timeout = 10; // sec

    if( (argc > 2) && (0 == strcmp( argv[1], "-s") ) )
    {
        if( argc < 3)
            usage();

        const char *key = argv[2];
        mqtt_udp_enable_signature( key, strnlen(key, PKT_BUF_SIZE) ); // here PKT_BUF_SIZE == too big
        check_sig = 1;

        argc -= 2;
        argv += 2;
    }

    if( argc != 3 )
        usage();

    topic = argv[1];
    msg   = argv[2];

    printf("Will listen to MQTT/UDP traffic and wait for PUBLISH '%s'='%s'\n", topic, msg );

    int fd, rc;

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

    start = time(0);

    while(1)
    {
        int rc = mqtt_udp_recv( fd, check_pkt );
        if( rc )
        {
            printf("mqtt_udp_recv err = %d\n", rc );
        }

        time_t now;
        now = time(0);
        if( now > start+timeout )
        {
            printf("timed ont, exit\n");
            exit(2);
        }

    }

    return 0;
}


