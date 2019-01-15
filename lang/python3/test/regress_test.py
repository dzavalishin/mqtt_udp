#!/usr/bin/env python3

'''
	Unit tests

'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine as me



def testPlain():
    assert( me.match("aaa/ccc/bbb", "aaa/ccc/bbb") );
    assert( not me.match("aaa/ccc/bbb", "aaa/c/bbb") );
    assert( not me.match("aaa/ccc/bbb", "aaa/ccccc/bbb") );
    assert( not me.match("aaa/ccc/bbb", "aaa/ccccc/ccc") );
    print( "\ttestPlain PASSED" )


def testPlus():
    assert( me.match("aaa/+/bbb", "aaa/ccc/bbb") );
    assert( me.match("aaa/+/bbb", "aaa/c/bbb") );
    assert( me.match("aaa/+/bbb", "aaa/ccccc/bbb") );
    assert( not me.match("aaa/+/bbb", "aaa/ccccc/ccc") );
    print( "\ttestPlus PASSED" )

def testSharp():
    assert( me.match("aaa/#", "aaa/ccc/bbb") );
    assert( me.match("aaa/#", "aaa/c/bbb") );
    assert( me.match("aaa/#", "aaa/ccccc/bbb") );
    assert( not me.match("aaa/#", "aba/ccccc/ccc") );
    print( "\ttestSharp PASSED" )



if __name__ == "__main__":
    print( "Will run Unit tests" )

    testPlain()
    testPlus()
    testSharp()

