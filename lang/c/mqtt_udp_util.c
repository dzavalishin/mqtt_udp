/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Util funcs
 *
**/

#include <sys/types.h>
//#include <string.h>
#include <stdio.h>
//#include <stdlib.h>
//#include <locale.h>
//#include <errno.h>

#include "mqtt_udp.h"


// todo ctype

void mqtt_udp_dump( const char *buf, size_t len )
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
}

