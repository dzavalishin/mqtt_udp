#include <sys/types.h>
//#include <sys/socket.h>
//#include <netinet/in.h>
//#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
//#include <unistd.h>
//#include <locale.h>
//#include <fcntl.h>
#include <errno.h>

#include "mqtt_udp_local.h"

#define BUFLEN 512



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


    //int net_tlen = htons( tlen );
    int net_tlen = tlen;
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



