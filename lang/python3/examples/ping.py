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

send_socket = mqttudp.pub.make_send_socket()

def ping_thread():
    while True:
        mqttudp.pub.send_ping( send_socket )
        time.sleep(1)



if __name__ == "__main__":
    print( "Will send MQTT/UDP ping packet and dump all the replies forever" )
    print( "Press ^C to stop" )

    recv_socket = mqttudp.sub.make_recv_socket()

#    send_socket = mqttudp.pub.make_send_socket()
    mqttudp.pub.send_ping( send_socket )

    pt = threading.Thread(target=ping_thread, args=())
    pt.start()


    while True:
        pkt = mqttudp.sub.recv_udp_packet( recv_socket )    
        ptype,topic,value,pflags = mqttudp.sub.parse_packet( pkt ) 

        if ptype == "publish":
            print( topic+"="+value )
        if ptype == "pingreq":
            print( ptype )
        if ptype == "pingresp":
            print( ptype )
