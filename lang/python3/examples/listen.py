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

import mqttudp.engine

last = {}


def recv_packet(ptype,topic,value,pflags,addr):
#    print( topic + "=" + value + str(addr) )
    if ptype != "publish":
        print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    if last.__contains__(topic) and last[topic] == value:
        return
    last[topic] = value
    print( topic+"="+value )



if __name__ == "__main__":
    print( "Will dump MQTT/UDP packets with changed value" )

    mqttudp.engine.listen(recv_packet)

"""
    s = mqttudp.engine.make_recv_socket()
    last = {}
    while True:
        pkt = mqttudp.engine.recv_udp_packet(s)    
        ptype,topic,value,pflags = mqttudp.engine.parse_packet(pkt)
        if ptype == "ping":
            print( "Got ping" )
            continue
        if ptype != "publish":
            continue
        if last.__contains__(topic) and last[topic] == value:
            continue
        last[topic] = value
        print( topic+"="+value )
"""
