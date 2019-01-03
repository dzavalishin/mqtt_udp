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

send_socket = mqttudp.engine.make_send_socket()

def ping_thread():
    while True:
        mqttudp.engine.send_ping( send_socket )
        time.sleep(1)



def recv_packet(ptype,topic,value,pflags,addr):
#    print( topic + "=" + value + str(addr) )
    if ptype != "publish":
        print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    print( topic+"="+value+ "\t\t" + str(addr) )



if __name__ == "__main__":
    print( "Will send MQTT/UDP ping packet and dump all the replies forever" )
    print( "Press ^C to stop" )

    recv_socket = mqttudp.engine.make_recv_socket()

#    send_socket = mqttudp.engine.make_send_socket()
    mqttudp.engine.send_ping( send_socket )

    pt = threading.Thread(target=ping_thread, args=())
    pt.start()

    mqttudp.engine.listen(recv_packet)

"""
    while True:
        pkt = mqttudp.engine.recv_udp_packet( recv_socket )    
        ptype,topic,value,pflags = mqttudp.engine.parse_packet( pkt ) 

        if ptype == "publish":
            print( topic+"="+value )
        if ptype == "pingreq":
            print( ptype )
        if ptype == "pingresp":
            print( ptype )
"""






