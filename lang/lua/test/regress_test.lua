#!/usr/bin/lua 

package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install

local me = require "mqttudp"

--- Regress test for match function
-- Tests plain comparison, with no wildcards

function testPlain()
    print( "\ttestPlain" )
    assert( me.match("aaa/ccc/bbb", "aaa/ccc/bbb") );
    assert( not me.match("aaa/ccc/bbb", "aaa/c/bbb") );
    assert( not me.match("aaa/ccc/bbb", "aaa/ccccc/bbb") );
    assert( not me.match("aaa/ccc/bbb", "aaa/ccccc/ccc") );
    print( "\ttestPlain PASSED" )
end

--- Regress test for match function
-- Tests '+' wildcard

function testPlus()
    print( "\ttestPlus" )
    assert( me.match("aaa/+/bbb", "aaa/ccc/bbb") );
    assert( me.match("aaa/+/bbb", "aaa/c/bbb") );
    assert( me.match("aaa/+/bbb", "aaa/ccccc/bbb") );
    assert( not me.match("aaa/+/bbb", "aaa/ccccc/ccc") );
    print( "\ttestPlus PASSED" )
end

--- Regress test for match function
-- Tests '#' wildcard

function testSharp()
    print( "\ttestSharp" )
    assert( me.match("aaa/#", "aaa/ccc/bbb") );
    assert( me.match("aaa/#", "aaa/c/bbb") );
    assert( me.match("aaa/#", "aaa/ccccc/bbb") );
    assert( not me.match("aaa/#", "aba/ccccc/ccc") );
    print( "\ttestSharp PASSED" )
end




print( "Will run Unit tests" )

testPlain()
testPlus()
testSharp()

