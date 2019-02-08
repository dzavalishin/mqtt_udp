/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * Copyright (C) 2017-2018 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Utility and error handling functions
 *
**/

#include "config.h"

#include <stdio.h>

#include "mqtt_udp.h"


// todo ctype

/**
 * 
 * @brief Dump binary data
 * 
 * @param buf Data to dump
 * @param len Size of buffer in bytes
 * 
**/

void mqtt_udp_dump( const char *buf, size_t len )
{
    int i;

    for(i = 0; i < len; i++)
    {
        printf("0x%02x ", 0xFFu & buf[i]);
    }
    printf("\n");

    for(i = 0; i < len; i++)
    {
        printf("%c", ((buf[i] > ' ') && (buf[i] < 0x7F)) ? buf[i] : '.'   );
    }
    printf("\n");
}





static err_func_t *user_error_handler = 0;

/**
 * @brief Set user error handler
 * 
 * Library code does not print errors, but calls global
 * error handler. Default handler, though, **prints**
 * error messages.
 * 
 * User can set his own handler. Note that handler must
 * return its err_no parameter or 0 if it wants caller
 * to ignore error.
 * 
**/
void mqtt_udp_set_error_handler( err_func_t *handler )
{
    user_error_handler = handler;
}

/**
 * 
 * @brief Called in case of error by library functions
 * 
 * if there is user error handler installed, calls it. Else
 * just prints error message.
 * 
 * Can be called by user code too.
 * 
**/
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







