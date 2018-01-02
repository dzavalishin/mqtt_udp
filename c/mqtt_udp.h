#ifndef MQTT_UDP_H
#define MQTT_UDP_H

#include <stdlib.h>
#include "mqtt_udp_defs.h"

#ifdef __cplusplus
extern "C" {
#endif


int mqtt_udp_socket(void);
int mqtt_udp_bind( int fd ); // prepare to receive data

int mqtt_udp_send( int fd, char *topic, char *data );


//void (*mqtt_udp_handle_recv)( char *topic, char *data );
typedef void (*mqtt_udp_handle_recv)( char *, char * );

int mqtt_udp_recv( int fd, mqtt_udp_handle_recv handle );



// Was in local.h, use with caution


int mqtt_udp_send_pkt( int fd, char *data, size_t len );


int mqtt_udp_recv_pkt( int fd, unsigned char *buf, size_t buflen );

int mqtt_udp_parse_pkt( const char *pkt, size_t plen, char *topic, size_t o_tlen, char *value, size_t o_vlen );


#ifdef __cplusplus
}
#endif

#endif // MQTT_UDP_H
