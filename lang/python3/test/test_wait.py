#!/usr/bin/env python3

'''
	NB! Used in regress tests.

        Listen to all the traffic on MQTT/UDP, 
        wait for given topic/msg, exit on timeout

'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import time

import mqttudp.engine

__TIMEOUT = 4

start_time = None

#need_topic = None
#need_value = None

def recv_packet(pkt):
    global need_topic, need_value
    if pkt.ptype != mqttudp.engine.PacketType.Publish:
        #print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    #print( topic+"="+value+ "\t\t" + str(addr) )

    if (need_topic == pkt.topic) and (need_value == pkt.value):
        print("Got it!")
        sys.exit(0)

    now = int(time.time())

    if now > start_time+__TIMEOUT:
        print("Timeout")
        sys.exit(1)



if __name__ == "__main__":
    global need_topic, need_value
    need_topic = sys.argv[1]
    need_value = sys.argv[2]
    print( "Will wait for "+need_topic+" = "+need_value )

    start_time = int(time.time())

    mqttudp.engine.listen(recv_packet)

