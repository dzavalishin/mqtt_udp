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


int check_pkt(struct mqtt_udp_pkt *pkt)
{
    if(
       (0 == strcmp( topic, pkt->topic ))
       &&
       (0 == strcmp( msg, pkt->value ))
      )
    {
        printf("Got it!\n");
        exit(0);
    }

    return 0;
}


int main(int argc, char *argv[])
{
    time_t start, now;

    long timeout = 10; // sec

    if( argc != 3 )
    {
        printf("Will listen to MQTT/UDP traffic and wait for message\n");
        printf("Usage: waitmsg topic data\n\n");
        exit(33);
    }

    topic = argv[1];
    msg   = argv[2];

    printf("Will listen to MQTT/UDP traffic and wait for PUBLISH '%s'='%s'\n", topic, msg );

    int fd, rc;
    //char buf[PKT_BUF_SIZE];

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

        now = time(0);
        if( now > start+timeout )
        {
            printf("timed ont, exit\n");
            exit(1);
        }

    }

    return 0;
}


