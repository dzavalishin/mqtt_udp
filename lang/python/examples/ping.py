#!/bin/python

'''
	This program will send PING packet and wait for replies
'''

# will work even if package is not installed
import sys
sys.path.append('..')

#import threading
import mqttudp.pub
import mqttudp.sub
#import random
import time



if __name__ == "__main__":
    udp_socket = mqttudp.pub.make_send_socket()
    mqttudp.pub.send_ping(udp_socket)

    s = mqttudp.sub.make_recv_socket()
    while True:
        pkt = mqttudp.sub.recv_udp_packet(s)    
        ptype,topic,value = mqttudp.sub.parse_packet(pkt)

        if ptype == "publish":
            print topic+"="+value
        if ptype == "pingreq":
            print ptype
        if ptype == "pingresp":
            print ptype
