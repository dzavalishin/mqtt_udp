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

#include "config.h"

//#include <sys/types.h>
#include <stdio.h>

#include "mqtt_udp.h"


// todo ctype

void mqtt_udp_dump( const char *buf, size_t len )
{
    int i;

    for(i = 0; i < len; i++)
    {
        printf("0x%x ", buf[i]);
    }
    printf("\n");

    for(i = 0; i < len; i++)
    {
        printf("%c", ((buf[i] > ' ') && (buf[i] < 0x7F)) ? buf[i] : '.'   );
    }
    printf("\n");
}




//typedef void err_func_t( int , char * , char * );

static err_func_t *user_error_handler = 0;


void mqtt_udp_set_error_handler( err_func_t *handler )
{
    user_error_handler = handler;
}

int mqtt_udp_global_error_handler( mqtt_udp_err_t type, int err_no, char *msg, char *arg )
{
    if( user_error_handler )
    {
        return user_error_handler( type, err_no, msg, arg );
    }

    if( arg == 0 ) arg = "";

    if( err_no )
        printf("%s%s, err_no = %d\n", msg, arg, err_no );
    else
        printf("%s%s\n", msg, arg );

    return err_no;
}







