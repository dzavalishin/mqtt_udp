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




int mqtt_udp_recv_pkt( int fd, unsigned char *buf, size_t buflen )
{
    struct sockaddr_in addr;

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

    int slen = sizeof(addr);

    memset(buf, 0, sizeof(buf));

    recvfrom(fd, buf, buflen, 0, (struct sockaddr *) &addr, &slen);

}
