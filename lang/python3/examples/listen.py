#!/usr/bin/env python3

'''
Created on 24.12.2017

@author: dz

Listen to all the traffic on MQTT/UDP, print changed data only
'''
# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import mqttudp.engine as me

last = {}


def recv_packet(pkt):
    if pkt.ptype != me.PacketType.Publish:
        print( str(pkt.ptype) + ", " + pkt.topic + "\t\t" + str(pkt.addr) )
        return
    if last.__contains__(pkt.topic) and last[pkt.topic] == pkt.value:
        return
    last[pkt.topic] = pkt.value
    print( pkt.topic+"="+pkt.value+ "\t\t" + str(pkt.addr) )



if __name__ == "__main__":
    print( "Will dump MQTT/UDP packets with changed value" )

    me.listen(recv_packet)

