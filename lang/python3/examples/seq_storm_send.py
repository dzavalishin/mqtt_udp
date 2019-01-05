#!/bin/python

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
#import random
import time

TOPIC="sequential_storm"


if __name__ == "__main__":

    if (sys.argv.__len__() == 3) and (sys.argv[1] == "-s"):
        speed = int( sys.argv[2] )
        sleep_time = 1.0 / (speed+0.0)
        #print(sleep_time)
    else:
        sleep_time = 0

    print( "Will send MQTT/UDP packets with sequential as a payload, topic is '"+TOPIC+"'" )

    if sleep_time > 0.0:
        print( "\nNB! This program will attempt to send "+str(speed)+" packets per second" )
    else:
        print( "\nNB! This program sends data as fast as possible, you're WARNED!" )
        print( "Use -s packets_per_sec to limit send speed" )

    _ = input( "\nStart seq_storm_check and press ENTER to continue...")

    n = 0

    while True:
        if (n % 10000) == 0:
            print( "Send ", n )
        mqttudp.engine.send_publish_packet( TOPIC, str(n) )
        n = n+1
        if sleep_time > 0:
            time.sleep( sleep_time )



