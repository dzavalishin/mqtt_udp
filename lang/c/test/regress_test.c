
#include <stdio.h>
#include <string.h>

#include "../mqtt_udp.h"


void assertTrue( int i )
{
    if( !i )
    {
        printf("FAILED!\n");
        exit(33);
    }
}

void assertFalse( int i ) { assertTrue( !i ); }




void testPlain()
{
    char * tf = "aaa/ccc/bbb";
    printf("\ttest Plain ");
    assertTrue( mqtt_udp_match( tf, "aaa/ccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/c/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/ccccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/ccccc/ccc") );
    printf("PASSED\n");
}


void testPlus()
{
    char * tf = "aaa/+/bbb";
    printf("\ttest Plus ");
    assertTrue( mqtt_udp_match( tf, "aaa/ccc/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/c/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/ccccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/ccccc/ccc") );
    printf("PASSED\n");
}


void testSharp()
{
    char * tf = "aaa/#";
    printf("\ttest Sharp ");
    assertTrue( mqtt_udp_match( tf, "aaa/ccc/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/c/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/ccccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aba/ccccc/ccc") );
    printf("PASSED\n");
}

static char expected[] = 
{
    0xd0, 0xca, 0x61, 0x77, 
    0xc6, 0x1c, 0x97, 0x5f, 
    0xd2, 0xf8, 0xc0, 0x7d, 
    0x8c, 0x65, 0x28, 0xc6
};

void testHMAC()
{
    const char key[] = "key";
    unsigned char text[] = "text";
    unsigned char hmac[MD5_DIGEST_SIZE];
    
    printf("\ttest HMAC ");

    //assertFalse( mqtt_udp_enable_signature( key, strlen(key) ) );
    assertFalse( mqtt_udp_enable_signature( key, (sizeof key) - 1 ) );
    assertTrue((int)mqtt_udp_hmac_md5);

    //mqtt_udp_hmac_md5( text, strlen(text), hmac );
    mqtt_udp_hmac_md5( text, (sizeof text) - 1, hmac );
    
    //printf("\n");
    //mqtt_udp_dump( expected, sizeof(expected) );
    //mqtt_udp_dump( hmac, sizeof(hmac) );
    assertFalse( memcmp( hmac, expected, sizeof(expected)) );
    //assertFalse( memcmp( hmac, expected, MD5_DIGEST_SIZE) );

    printf("PASSED\n");
}

int main()
{

    testPlain();
    testPlus();
    testSharp();

    testHMAC();
}


