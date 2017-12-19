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

#include "mqtt_udp_local.h"


#define BUFLEN 64

int main(int argc, char *argv[])
{
    int fd;
    unsigned char buf[BUFLEN];

    fd = mqtt_udp_socket();

    if(fd < 0) {
        perror("socket");
        exit(1);
    }

    memset(buf, 0, sizeof(buf));

    int rc = mqtt_udp_recv_pkt( fd, buf, BUFLEN );


    printf("Recieved!\n");



    for (int i=0; i<BUFLEN;i++)
    {
        printf("0x%x ", buf[i]);
    }
    printf("\n");

    for (int i=0; i<BUFLEN;i++)
    {
        printf("%c", ((buf[i] > ' ') && (buf[i] < 0x7F)) ? buf[i] : '.'   );
    }
    printf("\n");


    char topic[BUFLEN];
    char value[BUFLEN];

    rc = mqtt_udp_parse_pkt( buf, BUFLEN, topic, BUFLEN, value, BUFLEN );
    if( rc )
        printf("not parsed\n");
    else
        printf("'%s' = '%s'\n", topic, value );

    return 0;
}
