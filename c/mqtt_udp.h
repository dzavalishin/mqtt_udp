#ifndef MQTT_UDP_H
#define MQTT_UDP_H

#include <stdlib.h>


int mqtt_udp_socket(void);

int mqtt_udp_send( int fd, char *topic, char *data );


//void (*mqtt_udp_handle_recv)( char *topic, char *data );
typedef void (*mqtt_udp_handle_recv)( char *, char * );

int mqtt_udp_recv( int fd, mqtt_udp_handle_recv handle );


#endif // MQTT_UDP_H
