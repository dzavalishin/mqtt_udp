#!/usr/bin/env python3

'''
	Unit tests

'''
import unittest

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine as me

 
class MatchTest(unittest.TestCase):
    def testPlain(self):
        self.assertTrue( me.match("aaa/ccc/bbb", "aaa/ccc/bbb") );
        self.assertFalse( me.match("aaa/ccc/bbb", "aaa/c/bbb") );
        self.assertFalse( me.match("aaa/ccc/bbb", "aaa/ccccc/bbb") );
        self.assertFalse( me.match("aaa/ccc/bbb", "aaa/ccccc/ccc") );
        print( "\ttestPlain PASSED" )
    
    
    def testPlus(self):
        self.assertTrue( me.match("aaa/+/bbb", "aaa/ccc/bbb") );
        self.assertTrue( me.match("aaa/+/bbb", "aaa/c/bbb") );
        self.assertTrue( me.match("aaa/+/bbb", "aaa/ccccc/bbb") );
        self.assertFalse( me.match("aaa/+/bbb", "aaa/ccccc/ccc") );
        print( "\ttestPlus PASSED" )
    
    def testSharp(self):
        self.assertTrue( me.match("aaa/#", "aaa/ccc/bbb") );
        self.assertTrue( me.match("aaa/#", "aaa/c/bbb") );
        self.assertTrue( me.match("aaa/#", "aaa/ccccc/bbb") );
        self.assertFalse( me.match("aaa/#", "aba/ccccc/ccc") );
        print( "\ttestSharp PASSED" )

        
if __name__ == '__main__':
    unittest.main()
