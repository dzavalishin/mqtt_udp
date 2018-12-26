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
import mqttudp.pub
import random
import time

TOPIC="random_data"


if __name__ == "__main__":
    udp_socket = mqttudp.pub.make_send_socket()

    while True:
        n = str(random.randint(0, 9))
        print( "Send "+n )
        mqttudp.pub.send( udp_socket, TOPIC, n )
        time.sleep(2)




