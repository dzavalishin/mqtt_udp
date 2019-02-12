#!/usr/bin/env python3

'''
	NB! Used in regress tests. Do not modify!
	This program will publish one message. Topic is argv[1], value is argv[2]
'''

# will work even if package is not installed 
import argparse
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine as me

#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#	NB! Used in regress tests. Do not modify!
#
#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Send MQTT/UDP publish message',prog='test_pub')
    
    parser.add_argument('topic', help='topic to send to')
    parser.add_argument('value', help='value to be sent')
    parser.add_argument('-s', '--signature',  dest='signature', action='store', help='digital signature key')
    
    args = parser.parse_args()

    if args.signature != None:
        me.set_signature( args.signature )

    print( "Will publish to '"+sys.argv[1] + "' value '" + sys.argv[2] + "'")
    me.send_publish( args.topic, args.value )
    print( "Sent ok")
