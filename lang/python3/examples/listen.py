#!/usr/bin/env python3

'''
Created on 24.12.2017

@author: dz

Listen to all the traffic on MQTT/UDP, print changed data only
'''
# will work even if package is not installed
import argparse
import sys
sys.path.append('..')

import mqttudp.engine as me

last = {}


def recv_packet(pkt):
    sigmsg = "     "
    if pkt.signed:
        sigmsg = "Sig! "

    if pkt.ptype != me.PacketType.Publish:
        print( sigmsg + str(pkt.ptype) + ", " + pkt.topic + "\t\t" + str(pkt.addr) )
        return
    if last.__contains__(pkt.topic) and last[pkt.topic] == pkt.value:
        return
    last[pkt.topic] = pkt.value
    print( sigmsg + pkt.topic+"="+pkt.value+ "\t\t" + str(pkt.addr) )



if __name__ == "__main__":
    #me.set_signature( "signPassword" )

    parser = argparse.ArgumentParser(description='Listen MQTT/UDP and print different messages',prog='listen')
    parser.add_argument('-s', '--signature',  dest='signature', action='store', help='digital signature key')
    
    args = parser.parse_args()

    if args.signature != None:
        me.set_signature( args.signature )
        print("Signature is '"+args.signature+"'")

    print( "Will print MQTT/UDP packets with changed value" )
    me.listen(recv_packet)

