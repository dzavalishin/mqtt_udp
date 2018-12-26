#!/bin/python

'''
	This program will send PING packet and wait for replies
'''

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import threading
import mqttudp.pub
import mqttudp.sub
import random
import time



if __name__ == "__main__":
    print( "Will send MQTT/UDP [ing packet and dump all the replies forever" )
    print( "Press ^C to stop" )

    recv_socket = mqttudp.sub.make_recv_socket()

    send_socket = mqttudp.pub.make_send_socket()
    mqttudp.pub.send_ping( send_socket )

    while True:
        pkt = mqttudp.sub.recv_udp_packet( recv_socket )    
        ptype,topic,value = mqttudp.sub.parse_packet( pkt ) 

        if ptype == "publish":
            print( topic+"="+value )
        if ptype == "pingreq":
            print( ptype )
        if ptype == "pingresp":
            print( ptype )
