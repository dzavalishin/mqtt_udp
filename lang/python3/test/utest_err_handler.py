#!/usr/bin/env python3

'''
	Unit tests: error handler

'''
import unittest

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine as me



error_trigger = False
error_type : me.ErrorType = None

def user_error_handler( retcode : int, etype : me.ErrorType, msg : str ):
    global error_trigger, error_type
    error_trigger = True
    error_type = etype
    return 22



class ErrorHandlerTest(unittest.TestCase):
    def testCall(self):
        self.assertEqual( 33, me.error_handler( 33, me.ErrorType.IO, "test error" ))
        self.assertEqual( None, error_type );
        print( "\ttestCall PASSED" )
    
    
    def testHandle(self):
        me.set_error_handler( user_error_handler )
        self.assertEqual( 22, me.error_handler( 44, me.ErrorType.Protocol, "test error" ))
        self.assertEqual( me.ErrorType.Protocol, error_type );
        print( "\ttestHandle PASSED" )

    
    #def testSharp(self):
    #    self.assertTrue( me.match("aaa/#", "aaa/ccccc/bbb") );
    #    self.assertFalse( me.match("aaa/#", "aba/ccccc/ccc") );
    #    print( "\ttestSharp PASSED" )

        
if __name__ == '__main__':
    unittest.main()
