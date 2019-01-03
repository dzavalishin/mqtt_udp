#!/bin/python

'''
	This program will generate random data and send to MQTT/UDP
	topic once a 5 sec
'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import threading
import mqttudp.engine
import random
import time

TOPIC="sequential_storm"


if __name__ == "__main__":
    print( "Will send MQTT/UDP packets with sequential as a payload, topic is '"+TOPIC+"'" )
    print( "\nNB! This program sends data as fast as possible, you're WARNED!" )
    input( "\nStart seq_storm_check and press ENTER to continue...")

    udp_socket = mqttudp.engine.make_send_socket()

    n = 0

    while True:
        if (n % 10000) == 0:
            print( "Send ", n )
        mqttudp.engine.send( udp_socket, TOPIC, str(n) )
        n = n+1




