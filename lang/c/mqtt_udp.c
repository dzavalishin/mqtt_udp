/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Network glue code 
 *
**/


#include "config.h"
/*
#include <sys/types.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <locale.h>
#include <fcntl.h>
#include <errno.h>
*/
#include "mqtt_udp.h"
//#include "udp_io.h"


static int last_socket = -1;


int mqtt_udp_get_send_fd( void ) // TODO hack, get fd to send datagrams
{
    if( last_socket < 0 )
        last_socket = mqtt_udp_socket(); // TODO not thread safe
    return last_socket;
}





