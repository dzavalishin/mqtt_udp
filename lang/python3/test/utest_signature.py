#!/usr/bin/env python3

'''
	Unit tests: signature

'''
import unittest

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine as me





class SignatureTest(unittest.TestCase):
    def testSign(self):
        #self.assertEqual( 33, me.error_handler( 33, me.ErrorType.IO, "test error" ))
        #self.assertEqual( None, error_type );
        me.set_signature( "key" )
        sig = me.sign_data( "text" )
        hex = sig #.encode("ascii").hex()
        print( str(type(hex)) )
        self.assertEqual( "d0ca6177c61c975fd2f8c07d8c6528c6", hex )
        print( "\ttest Sign PASSED" )
    
    
    #def testHandle(self):
    #    me.set_error_handler( user_error_handler )
    #    self.assertEqual( 22, me.error_handler( 44, me.ErrorType.Protocol, "test error" ))
    #    self.assertEqual( me.ErrorType.Protocol, error_type );
    #    print( "\ttestHandle PASSED" )

    
    #def testSharp(self):
    #    self.assertTrue( me.match("aaa/#", "aaa/ccccc/bbb") );
    #    self.assertFalse( me.match("aaa/#", "aba/ccccc/ccc") );
    #    print( "\ttestSharp PASSED" )

        
if __name__ == '__main__':
    unittest.main()
