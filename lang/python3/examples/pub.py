#!/usr/bin/env python3

'''
	This program will publish one message. Topic is argv[1], value is argv[2]
'''

# will work even if package is not installed 
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import mqttudp.engine as me

if __name__ == "__main__":
    #me.set_signature( "signPassword" )
    me.send_publish( sys.argv[1], sys.argv[2] )
