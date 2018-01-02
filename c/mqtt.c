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
//#define IP      "192.168.0.255"

int main(int argc, char *argv[])
{
    struct sockaddr_in addr;
    int fd;
    unsigned char buf[BUFLEN];
    //unsigned char message[]={0x44, 0x69, 0x73, 0x63, 0x6f, 0x76, 0x65, 0x72, 0x79, 0x3a, 0x20, 0x57, 0x68, 0x6f, 0x20, 0x69, 0x73, 0x20, 0x6f, 0x75, 0x74, 0x20, 0x74, 0x68, 0x65, 0x72, 0x65, 0x3f, 0x00, 0x0a};

    if ((fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("socket");
        exit(1);
    }


    {
        struct sockaddr_in srcaddr;

        memset(&srcaddr, 0, sizeof(srcaddr));

        srcaddr.sin_family = AF_INET;
        srcaddr.sin_addr.s_addr = htonl(INADDR_ANY);
        srcaddr.sin_port = htons(MQTT_PORT);

        if (bind(fd, (struct sockaddr *) &srcaddr, sizeof(srcaddr)) < 0) {
            perror("bind");
            exit(1);
        }
    }

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    //addr.sin_addr.s_addr = inet_addr(IP);
    //addr.sin_port = htons(MQTT_PORT);


    if(0)
    {
    int broadcast=1;
    setsockopt(fd, SOL_SOCKET, SO_BROADCAST, &broadcast, sizeof(broadcast));
    }

    int slen = sizeof(addr);

    memset(buf, 0, sizeof(buf));

    recvfrom(fd, buf, BUFLEN, 0, (struct sockaddr *) &addr, &slen);
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


    return 0;
}


#if 0

int mqtt_udp_socket(void)
{
    int fd;

    if ((fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0)
    {
        perror("socket");
        exit(1);
        return -1;
    }

    {
        int broadcast=1;
        setsockopt(fd, SOL_SOCKET, SO_BROADCAST, &broadcast, sizeof(broadcast));
    }

}



int mqtt_udp_send_pkt( int fd, char *data, size_t len )
{
    struct sockaddr_in addr;

    struct sockaddr_in serverAddr;
    socklen_t addr_size;

    /*Configure settings in address struct*/
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons( MQTT_PORT );
    serverAddr.sin_addr.s_addr = inet_addr("255.255.255.255");
    memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);

    addr_size = sizeof serverAddr;

    ssize_t rc = sendto( fd, data, len, 0, (struct sockaddr *)&serverAddr, addr_size);

    return (rc != len) ? EIO : 0;
}


static int pack_len( char *buf, int *blen, int *used, int data_len )
{
    *used = 0;
    while( 1 )
    {
        if( *blen <= 0 ) return ENOMEM;

        int byte = data_len % 128;
        data_len /= 128;

        if( data_len > 0 )
            byte |= 0x80;

        *buf++ = byte;
        (*blen)--;
        (*used)++;

        if( data_len == 0 ) return 0;
    }
}


int mqtt_udp_send( int fd, char *topic, char *data )
{
    unsigned char buf[BUFLEN];

    int tlen = strlen(topic);
    int dlen = strlen(data);

    int blen = sizeof(buf);
    unsigned char *bp = buf;

    *bp++ = PTYPE_PUBLISH;
    blen--;

    int total = tlen + dlen + 2; // packet size
    if( total > blen )
        return ENOMEM;

    //int size = total+1;

    int used = 0;
    int rc = pack_len( bp, &blen, &used, total );
    if( rc ) return rc;

    bp += used;


    int net_tlen = htons( tlen );
    *bp++ = (net_tlen >>8) & 0xFF;
    *bp++ = net_tlen & 0xFF;
    blen -= 2;

    //NB! Must be UTF-8
    while( tlen-- > 0 )
    {
        if( blen <= 0 ) return ENOMEM;
        *bp++ = *topic++;
        blen--;
    }

    while( dlen-- > 0 )
    {
        if( blen <= 0 ) return ENOMEM;
        *bp++ = *data++;
        blen--;
    }

    return mqtt_udp_send_pkt( fd, buf, bp-buf );
}

#endif

