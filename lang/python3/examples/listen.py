#!/bin/python

'''
Created on 24.12.2017

@author: dz

Listen to all the traffic on MQTT/UDP, print changed data only
'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.sub

if __name__ == "__main__":
    print( "Will dump MQTT/UDP packets with changed value" )
    s = mqttudp.sub.make_recv_socket()
    last = {}
    while True:
        pkt = mqttudp.sub.recv_udp_packet(s)    
        ptype,topic,value,pflags = mqttudp.sub.parse_packet(pkt)
        if ptype == "ping":
            print( "Got ping" )
            continue
        if ptype != "publish":
            continue
        if last.__contains__(topic) and last[topic] == value:
            continue
        last[topic] = value
        print( topic+"="+value )
