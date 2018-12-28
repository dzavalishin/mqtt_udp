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

import time

import mqttudp.sub



SEQ_STORM_TOPIC="sequential_storm"
STEP=10000

if __name__ == "__main__":

    print( "Will listen for MQTT/UDP packets with sequential number as a payload, topic is '"+SEQ_STORM_TOPIC+"'" )
    print( "\nStart seq_storm_send now...")

    s = mqttudp.sub.make_recv_socket()

    start_time = time.clock();

    last = 0
    errors = 0
    got = 0
    curr = 0

    while True:
        pkt = mqttudp.sub.recv_udp_packet(s)    
        ptype,topic,value = mqttudp.sub.parse_packet(pkt)

        if ptype != "publish":
            continue

        if topic != SEQ_STORM_TOPIC:
            continue

# report
        if (last % STEP) == 0:
            now = time.clock();
            speed_s = "?"
            if now != start_time:
                speed_s = '{:.0f}'.format( STEP/(now-start_time) )
            print("@ "+str(last)+"\terrors = "+str(errors)+"\tspeed is "+speed_s+" pkt/sec" )
            start_time = now

        curr = int( value )

        got = got + 1

        if curr == last:
            last = last + 1
            continue

        errors = errors + 1
        last = curr + 1
