#!/usr/bin/env python3

'''
	This program will generate random data and send to MQTT/UDP
	topic once a 5 sec
'''
# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

#import threading
import mqttudp.engine
import random
import time

TOPIC="random_data"


if __name__ == "__main__":
    print( "Will send MQTT/UDP packets with random number as a payload" )
    print( "Topic is '"+TOPIC+"'" )

    while True:
        n = str(random.randint(0, 9))
        print( "Send "+n )
        mqttudp.engine.send_publish_packet( TOPIC, n )
        time.sleep(2)


