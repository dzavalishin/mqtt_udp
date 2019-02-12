#!/usr/bin/env python3

'''
	This program will publish one message. Topic is argv[1], value is argv[2]
'''

# will work even if package is not installed 
import argparse
import sys
sys.path.append('..')
import mqttudp.engine as me

if __name__ == "__main__":
    #me.set_signature( "signPassword" )

    parser = argparse.ArgumentParser(description='Send MQTT/UDP publish message',prog='pub')
    #parser.add_argument('topic', metavar='T', nargs='1', help='topic to send to')
    #parser.add_argument('value', metavar='V', nargs='1', help='value to be sent')
    
    parser.add_argument('topic', help='topic to send to')
    parser.add_argument('value', help='value to be sent')
    parser.add_argument('-s', '--signature',  dest='signature', action='store', help='digital signature key')
    
    args = parser.parse_args()

    if args.signature != None:
        me.set_signature( args.signature )
        print("Signature is '"+args.signature+"'")

    print("Topic  is "+args.topic)
    print("Value  is "+args.value)

    me.send_publish( args.topic, args.value )
