#!/bin/python

'''
	This program will send PING packet and wait for replies
'''

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import threading
import mqttudp.engine
import random
import time



def ping_thread():
    while True:
        mqttudp.engine.send_ping()
        time.sleep(1)



def recv_packet(ptype,topic,value,pflags,addr):
    if ptype != "publish":
        print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    print( topic+"="+value+ "\t\t" + str(addr) )



if __name__ == "__main__":
    print( "Will send MQTT/UDP ping packet and dump all the replies forever" )
    print( "Press ^C to stop" )

    mqttudp.engine.send_ping()

    pt = threading.Thread(target=ping_thread, args=())
    pt.start()

    mqttudp.engine.listen(recv_packet)







