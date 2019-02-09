#!/usr/bin/env python3

'''
	NB! Used in regress tests. Do not modify!
	This program will publish one message. Topic is argv[1], value is argv[2]
'''

# will work even if package is not installed 
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine

#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#	NB! Used in regress tests. Do not modify!
#
#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


if __name__ == "__main__":
    print( "Will publish to '"+sys.argv[1] + "' value '" + sys.argv[2] + "'")
    mqttudp.engine.send_publish( sys.argv[1], sys.argv[2] )
    print( "Sent ok")
