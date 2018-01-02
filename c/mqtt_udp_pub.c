#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <locale.h>
#include <fcntl.h>
#include <errno.h>

#include "mqtt_udp.h"


#define BUFLEN 512

int main(int argc, char *argv[])
{
    int fd;
    unsigned char buf[BUFLEN];

    if( argc != 3 )
    {
        printf("usage: %s topic value", argv[0]);
        exit(3);
    }

    char *value = argv[2];
    char *topic = argv[1];

    printf("will publish '%s' to topic '%s'", value, topic );

    fd = mqtt_udp_socket();
    int rc = mqtt_udp_send_publish( fd, topic, value );

    if( rc )
        printf("error %d", rc);


    return rc;
}

