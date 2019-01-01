#ifndef MQTT_UDP_LOCAL_H
#define MQTT_UDP_LOCAL_H

#include "mqtt_udp.h"
#include "mqtt_udp_defs.h"

#error don't use me anymore


int mqtt_udp_send_pkt( int fd, char *data, size_t len );


int mqtt_udp_recv_pkt( int fd, unsigned char *buf, size_t buflen );

int mqtt_udp_parse_pkt( const char *pkt, size_t plen, char *topic, size_t o_tlen, char *value, size_t o_vlen );


#endif // MQTT_UDP_LOCAL_H
