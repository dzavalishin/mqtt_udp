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

void about( void );

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//
// NB! Used in regress tests! Do not modify!
//
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


int main(int argc, char *argv[])
{
    if( (argc != 3) && (argc != 5) )        
        about();    

    if( argc == 5 )
    {
        if( strcmp( argv[1], "-s") )
            about();

        const char *key = argv[2];
        mqtt_udp_enable_signature( key, strnlen(key, PKT_BUF_SIZE) ); // PKT_BUF_SIZE = just some limit

        argv += 2;
        argc -= 2;
    }

    char *value = argv[2];
    char *topic = argv[1];

#if 0
    const char *key = "signPassword";
    mqtt_udp_enable_signature( key, strlen(key) );
#endif

    printf("Will publish '%s' to topic '%s'\n", value, topic );

    int rc = mqtt_udp_send_publish( topic, value );

    if( rc )
        printf("error %d", rc);

    printf("Sent ok\n" );

    return rc;
}



void about( void )
{
        printf("Publish message to MQTT/UDP listeners\n\n");
        printf("Usage: mqtt_udp_pub [-s SignaturePassword] topic value");
        exit(3);
}