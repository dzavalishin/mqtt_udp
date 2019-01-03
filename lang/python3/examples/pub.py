#!/bin/python

'''
	This program will publish one message. Topic is argv[1], value is argv[2]
'''

# will work even if package is not installed 
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine


if __name__ == "__main__":
    udp_socket = mqttudp.engine.make_send_socket()
    mqttudp.engine.send_publish_packet( udp_socket, sys.argv[1], sys.argv[2] )
