
#include <stdio.h>

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
    printf("testPlain ");
    assertTrue( mqtt_udp_match( tf, "aaa/ccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/c/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/ccccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/ccccc/ccc") );
    printf("PASSED\n");
}


void testPlus()
{
    char * tf = "aaa/+/bbb";
    printf("testPlus ");
    assertTrue( mqtt_udp_match( tf, "aaa/ccc/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/c/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/ccccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aaa/ccccc/ccc") );
    printf("PASSED\n");
}


void testSharp()
{
    char * tf = "aaa/#";
    printf("testSharp ");
    assertTrue( mqtt_udp_match( tf, "aaa/ccc/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/c/bbb") );
    assertTrue( mqtt_udp_match( tf, "aaa/ccccc/bbb") );
    assertFalse( mqtt_udp_match( tf, "aba/ccccc/ccc") );
    printf("PASSED\n");
}


int main()
{

    testPlain();
    testPlus();
    testSharp();


}


