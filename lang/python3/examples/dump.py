#!/usr/bin/env python3

'''
Created on 26.12.2018

@author: dz

Listen to all the traffic on MQTT/UDP, print
'''
# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import mqttudp.engine as me


def recv_packet(pkt):
    if pkt.ptype != me.PacketType.Publish:
        print( str(pkt.ptype) + ", " + pkt.topic + "\t\t" + str(addr) )
        return
    print( pkt.topic+"="+pkt.value+ "\t\t" + str(pkt.addr) )



if __name__ == "__main__":
    print( "Will dump all MQTT/UDP packets recv'd" )

    me.listen(recv_packet)
