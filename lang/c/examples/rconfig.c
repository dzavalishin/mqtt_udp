/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 * 
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

void init_rconfig( void );


int main(int argc, char *argv[])
{
    printf("Demo of MQTT/UDP passive remote configuration\n\n");

    //setvbuf( stdout, 0, _IONBF, 0 );

    init_rconfig();

    while(1)
    {
        // We need to start listen loop: remote config takes input from it
        int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );
        if( rc ) {
            mqtt_udp_global_error_handler( MQ_Err_Other, rc, "recv_loop error", 0 );
        }
    }

    return 0;
}



// Actual remotely configurable items
// We use .opaque to keep initial value
mqtt_udp_rconfig_item_t rconfig_list[] =
{
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Switch 1 topic",	"topic/sw1", .value.s = 0, .opaque.s = "sw1" },
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Switch 2 topic",	"topic/sw2", .value.s = 0, .opaque.s = "sw2" },
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Switch 3 topic",	"topic/sw3", .value.s = 0, .opaque.s = "sw3" },
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Switch 4 topic",	"topic/sw4", .value.s = 0, .opaque.s = "sw4" },
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Di 0 topic",	    "topic/di0", .value.s = 0, .opaque.s = "di0" },
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_TOPIC, "Di 1 topic",	    "topic/di1", .value.s = 0, .opaque.s = "di1" },

    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_OTHER, "MAC address", 	"net/mac",   .value.s = 0, .opaque.s = "020000000000" },

    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_INFO, "Switch 4 topic", "info/soft",   .value.s = 0, .opaque.s = "C RConfig Demo" },
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_INFO, "Switch 4 topic", "info/ver",    .value.s = 0, .opaque.s = "0.0.1" },
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_INFO, "Switch 4 topic", "info/uptime", .value.s = 0, .opaque.s = "0d 00:00:00" },  // DO NON MOVE OR ADD LINES ABOVE, inited by array index below

    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_OTHER, "Name", 		"node/name",     .value.s = 0, .opaque.s = "C Test Node" }, // TODO R/W
    { MQ_CFG_TYPE_STRING, MQ_CFG_KIND_OTHER, "Location", 	"node/location", .value.s = 0, .opaque.s = "None" }, // TODO R/W
};


// Will be parameter of mqtt_udp_rconfig_client_init()
int rconfig_list_size = sizeof(rconfig_list) / sizeof(mqtt_udp_rconfig_item_t);


static int rconfig_rw_callback( int pos, int write );

void init_rconfig( void )
{
    char *mac_string = "020404040000";

    rconfig_list[8].value.s = "00:00:00";
    rconfig_list[9].value.s = "?";

    int rc = mqtt_udp_rconfig_client_init( mac_string, rconfig_rw_callback, rconfig_list, rconfig_list_size );
    if( rc ) printf("rconfig init failed, %d\n", rc );
}


// -----------------------------------------------------------------------
//
// Callback to connect to host code
//
// -----------------------------------------------------------------------


// Must read from local storage or write to local storage
// config item at pos
static int rconfig_rw_callback( int pos, int write )
{
    //printf("asked to %s item %d\n", write ? "save" : "load", pos );

    if( (pos < 0) || (pos >= rconfig_list_size) ) return -1; // TODO error

    if( rconfig_list[pos].type != MQ_CFG_TYPE_STRING )
        return -2;

    if( rconfig_list[pos].kind == MQ_CFG_KIND_INFO )
        return -3;

    if( write )
    {
        // RConfig got new setting from outside, store and use it
        printf("Got new settings for %s = '%s'\n", 
            rconfig_list[pos].topic,
            rconfig_list[pos].value.s
        );
    }
    else
    {
        // RConfig asks saved or default setting for item
        if( rconfig_list[pos].opaque.s != 0 )
        {
            mqtt_udp_rconfig_set_string( pos, rconfig_list[pos].opaque.s );
        }
    }

    return 0;

}





