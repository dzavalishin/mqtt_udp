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

import mqttudp.engine


def recv_packet(ptype,topic,value,pflags,addr):
    if ptype != "publish":
        print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    print( topic+"="+value+ "\t\t" + str(addr) )



if __name__ == "__main__":
    print( "Will dump all MQTT/UDP packets recv'd" )

    mqttudp.engine.listen(recv_packet)
