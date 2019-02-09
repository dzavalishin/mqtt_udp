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
 * @brief Example program: Send MQTT/UDP publish message
 *
**/

#include "config.h"

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>

#include "../mqtt_udp.h"


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//
// NB! Used in regress tests! Do not modify!
//
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


int main(int argc, char *argv[])
{
    if( argc != 3 )
    {
        printf("Publish message to MQTT/UDP listeners\n\n");
        printf("Usage: %s topic value", argv[0]);
        exit(3);
    }

    char *value = argv[2];
    char *topic = argv[1];

    printf("Will publish '%s' to topic '%s'\n", value, topic );

    int rc = mqtt_udp_send_publish( topic, value );

    if( rc )
        printf("error %d", rc);

    printf("Sent ok\n" );

    return rc;
}

