#include <stdio.h>

#include "hmac.h"


char tohex( int nibble )
{
    nibble &= 0x0F;
    if( nibble <= 9 ) return '0'+nibble;
    else return 'A'-10+nibble;
}

void phex( char c )
{
    printf("%c%c", tohex( c >> 4 ), tohex( c ) );
}


void dump( char *data, int len )
{
    int i;
    for( i = 0; i < len; i++ )
    {
        char c = data[i];
        phex( c );
    }
	printf("\n");
}

int main()
{
    char key[] = "key";
    char text[] = "text";

    //char out[SHA256_DIGEST_SIZE];
    char out[MD5_DIGEST_SIZE];


    //hmac_sha256(
    hmac_md5(
                key, sizeof( key ) - 1,
                text, sizeof( text ) - 1,
                out
               );

	//dump( key, sizeof(key) );

	dump( out, sizeof(out) );

}


