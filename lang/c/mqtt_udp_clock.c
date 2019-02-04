/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Example program: Translate current time and date to listeners
 *
**/

#include "config.h"

#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <time.h>

#include "mqtt_udp.h"


//#define TOPIC "$SYS/GMT"
/// Topic name to publish to
#define TOPIC "$SYS/localtime"


void usage( void )
{
    printf("Publish time/date message to MQTT/UDP listeners as topic '%s'\n\n", TOPIC );
    printf("Usage: mqtt_udp_clock [-f][-s]\n");
    printf("\t-f - full speed, send as fast as possible\n");
    printf("\t-s - silent\n");
}


int main(int argc, char *argv[])
{
    int fd;
    int fast = 0;
    int silent = 0;

    if( argc > 3 )
    {
        printf("\nWrong parameters\n\n");
        usage();
        exit(3);
    }

    if( argc == 2 )
    {
        if( 0 == strcmp( argv[1], "-f" ) )
            fast = 1;
        else if( 0 == strcmp( argv[1], "-s" ) )
            silent = 1;
        else
        {
            printf("\nWrong parameters\n\n");
            usage();
            exit(3);
        }
    }


    if(!silent)
        usage();

    if(fast)
        mqtt_udp_set_throttle( 0 ); // turn off speed limit


    while(1)
    {
        time_t timer;
        char buffer[26];
        struct tm* tm_info;

        time(&timer);
        tm_info = localtime(&timer);

        strftime(buffer, 26, "%Y/%m/%d %H:%M:%S", tm_info);

        if( !silent )
            printf("send: %s\n", buffer );

        int rc = mqtt_udp_send_publish( TOPIC, buffer );

        if( rc )
            printf("error %d", rc);

        if(!fast)
            sleep(60);
    }

    return 0;
}








