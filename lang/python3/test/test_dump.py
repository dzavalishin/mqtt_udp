#!/usr/bin/env python3

'''
	NB! Used in regress tests.

        Listen to all the traffic on MQTT/UDP, print
        Exit after N packets

'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine

counter = 4

def recv_packet(ptype,topic,value,pflags,addr):
    global counter
    if ptype != "publish":
        print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    print( topic+"="+value+ "\t\t" + str(addr) )
    counter -= 1
    if counter < 1:
        sys.exit(0)



if __name__ == "__main__":
    print( "Will dump all MQTT/UDP packets recv'd" )

    mqttudp.engine.listen(recv_packet)
