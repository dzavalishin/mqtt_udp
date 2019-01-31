/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * UDP packet reception for Nut/OS
 *
**/

//#include "../../config.h"
#include "config.h"
#include "../../mqtt_udp.h"
#include "udp_io.h"


int mqtt_udp_recv_pkt( int fd, char *buf, size_t buflen, uint32_t *src_ip_addr )
{

#if DEBUG
    printf("MQTT/UDP recv pkt ");
#endif

    memset( buf, 0, buflen );

    uint32_t netorder_src_addr;
    uint16_t netorder_src_port;

    int rc = NutUdpReceiveFrom( (UDPSOCKET *) fd, &netorder_src_addr, &netorder_src_port, buf, buflen, NUT_WAIT_INFINITE );

    if( src_ip_addr ) *src_ip_addr = ntohl( netorder_src_addr );

#if DEBUG
    printf(" recv pkt ok\n");
#endif

    return rc;
}
