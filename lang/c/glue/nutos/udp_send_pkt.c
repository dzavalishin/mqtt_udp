/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * Network connection - send UDP packet. To be rewritten per target OS.
 *
**/

//#include "../../config.h"
#include "config.h"
#include "../../mqtt_udp.h"
#include "udp_io.h"



int mqtt_udp_send_pkt( int fd, char *data, size_t len )
{
#if DEBUG
    printf("MQTT/UDP broadcast pkt\n");
#endif

    int rc = NutUdpSendTo( (UDPSOCKET *) fd, 0xFFFFFFFF, MQTT_PORT, data, len );
    //int rc = NutUdpSendTo( (UDPSOCKET *) fd, 0xFFFFFFFF, htons( MQTT_PORT ), data, len );

#if DEBUG
    printf("MQTT/UDP broadcast rc = %d  len = %d\n", rc, len );
#endif

    return rc ? -5 : 0; // -5 = EIO
}


int mqtt_udp_send_pkt_addr( int fd, char *data, size_t len, uint32_t ip_addr )
{
#if DEBUG
    printf("MQTT/UDP sent pkt to\n");
#endif

    //int rc = NutUdpSendTo( (UDPSOCKET *) fd, htonl( ip_addr ), htons( MQTT_PORT ), data, len );
    // TODO test me, do I have to use htonl( ip_addr )?
    int rc = NutUdpSendTo( (UDPSOCKET *) fd, ip_addr, MQTT_PORT, data, len );

    return rc ? -5 : 0; // -5 = EIO
}



