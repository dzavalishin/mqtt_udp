#!/bin/python

'''
Created on 26.12.2018

@author: dz

Listen to all the traffic on MQTT/UDP, print
'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.sub

if __name__ == "__main__":
    s = mqttudp.sub.make_recv_socket()
    while True:
        pkt = mqttudp.sub.recv_udp_packet(s)    
        ptype,topic,value = mqttudp.sub.parse_packet(pkt)
        if ptype != "publish":
            continue
        print( topic+"="+value )
