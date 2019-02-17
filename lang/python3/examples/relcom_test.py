#!/usr/bin/env python3

'''
	This program will generate random data and send to MQTT/UDP
	topic once a 5 sec using a RELiablie COMmunications subsystem.
'''
# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

#import threading
import mqttudp.engine as me
#import mqttudp.rconfig as rcfg
import mqttudp.relcom as relcom

import threading
import random
import time

TOPIC="random_data"


def recv_thread():
    me.listen( relcom.recv_packet )

if __name__ == "__main__":
    print( "Will send MQTT/UDP QoS 2 packets with random number as a payload" )
    print( "Topic is '"+TOPIC+"'" )

    recv = threading.Thread(target=recv_thread, args=())
    recv.start()

    while True:
        n = str(random.randint(0, 9))
        print( "Send "+n )
        relcom.send_publish_qos( TOPIC, n, 2 )
        time.sleep(2)


