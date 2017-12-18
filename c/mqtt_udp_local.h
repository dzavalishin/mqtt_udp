#ifndef MQTT_UDP_LOCAL_H
#define MQTT_UDP_LOCAL_H

#include "mqtt_udp.h"


#define PTYPE_PUBLISH 0x30


#define MQTT_PORT    1883



int mqtt_udp_send_pkt( int fd, char *data, size_t len );


int mqtt_udp_recv_pkt( int fd, unsigned char *buf, size_t buflen );


#endif // MQTT_UDP_LOCAL_H
