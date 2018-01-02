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




int mqtt_udp_recv_pkt( int fd, unsigned char *buf, size_t buflen )
{
    struct sockaddr_in addr;
/*
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
*/
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    //addr.sin_addr.s_addr = inet_addr(IP);
    //addr.sin_port = htons(MQTT_PORT);

    int slen = sizeof(addr);

    memset(buf, 0, sizeof(buf));

    recvfrom(fd, buf, buflen, 0, (struct sockaddr *) &addr, &slen);

}


static size_t mqtt_udp_decode_size( char **pkt )
{
    size_t ret = 0;

    while(1)
    {
        unsigned char byte = **pkt; (*pkt)++;
        ret |= byte & ~0x80;

        if( (byte & 0x80) == 0 )
            return ret;

        ret <<= 7;
    }
}

static size_t mqtt_udp_decode_topic_len( const unsigned char *pkt )
{
    size_t ret = 0;

    ret = (pkt[0] << 8) | pkt[1];

    return ret;
}


// sanity check
#define MAX_SZ (16*1024)
#define CHEWED (pkt - pstart)

int mqtt_udp_parse_pkt( const char *pkt, size_t plen, char *topic, size_t o_tlen, char *value, size_t o_vlen )
{
    const char *pstart = pkt;

    if( plen <= 4 )
        return EINVAL;

    unsigned char type = *pkt++;

    // lower 4 bits must be 0 - covered below
    //if( (type & 0xF) != 0 )
    //    return EINVAL;

    if( type != PTYPE_PUBLISH )
        return EINVAL;

    size_t total = mqtt_udp_decode_size( (char **)&pkt );

    total += CHEWED;

    if( total > MAX_SZ )
        return EINVAL;

    if( total+1 > plen )
        return EINVAL;

    size_t tlen = mqtt_udp_decode_topic_len( pkt );
    pkt += 2;

    if( tlen > MAX_SZ )
        return EINVAL;

    if( CHEWED + tlen > total )
        return EINVAL;

    //tlen++; // strlcpy needs place for zero
    int tcopy = (tlen+1 > o_tlen) ? o_tlen : tlen+1;
    strlcpy( topic, pkt, tcopy );

    pkt += tlen;

    size_t vlen = total - CHEWED;
    if( vlen > MAX_SZ )
        return EINVAL;

    vlen++; // strlcpy needs place for zero
    int vcopy = (vlen > o_vlen) ? o_vlen : vlen;
    strlcpy( value, pkt, vcopy );

    //printf("total %d tlen %d vlen %d\n", total, tlen, vlen );

    return 0;
}

