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

static void dump( const char *buf );


#define BUFLEN 64

int main(int argc, char *argv[])
{
    int loop = 0;

    if( argc == 2 && (0==strcmp(argv[1], "-f")) )
        loop++;

    int fd, rc;
    unsigned char buf[BUFLEN];

    fd = mqtt_udp_socket();

    if(fd < 0) {
        perror("socket");
        exit(1);
    }

    rc = mqtt_udp_bind( fd );
    if(rc) {
        perror("bind");
        exit(1);
    }

    do {

        memset(buf, 0, sizeof(buf));
        rc = mqtt_udp_recv_pkt( fd, buf, BUFLEN );

#if 0
        printf("Recieved!\n");

#endif

        char topic[BUFLEN];
        char value[BUFLEN];

        rc = mqtt_udp_parse_pkt( buf, BUFLEN, topic, BUFLEN, value, BUFLEN );
        if( rc )
        {
            printf("not parsed\n");
            dump(buf);
        }
        else
            printf("'%s' = '%s'\n", topic, value );

    } while(loop);

    return 0;
}


static void dump( const char *buf )
{
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
}

