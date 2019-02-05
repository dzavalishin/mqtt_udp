#!/bin/python

'''
Created on 24.12.2017

@author: dz

	This program will:
		- subscribe to all the topics on a given MQTT broker 
		  and pump all the updates to MQTT/UDP environment,
		- listen to all traffic on MQTT/UDP, pump updates to MQTT broker.
'''

# will work even if package is not installed
import sys
sys.path.append('..')

import threading
import mqttudp.pub
import mqttudp.sub
import mqttudp.interlock
import paho.mqtt.client as broker

SUBSCRIBE_TOPIC="#"

MQTT_BROKER_HOST="smart."
#MQTT_BROKER_HOST="iot.eclipse.org"


udp_send_socket = mqttudp.pub.make_send_socket()

ilock = mqttudp.interlock.bidirectional(5)

def broker_on_connect(client, userdata, rc, unkn):  # @UnusedVariable
    print("Connected with result code "+str(rc))
    client.subscribe(SUBSCRIBE_TOPIC)

def broker_on_message(client, userdata, msg):  # @UnusedVariable
    #print( msg )
    if ilock.broker_to_udp(msg.topic, msg.payload):
        mqttudp.pub.send( udp_send_socket, msg.topic, msg.payload )
        print("To UDP: "+msg.topic+"="+str(msg.payload))
    else:
        print("BLOCKED to UDP: "+msg.topic+"="+str(msg.payload))





def broker_listen_thread(bclient):
    bclient.loop_forever()

def udp_listen_thread(bclient):
    s = mqttudp.sub.make_recv_socket()
    last_message = {}
    while True:
        packet = mqttudp.sub.recv_udp_packet(s)    
        ptype,topic,value = mqttudp.sub.parse_packet(packet)
        if ptype != "publish":
            continue
        if last_message.has_key(topic) and last_message[topic] == value:
            continue
        last_message[topic] = value
        if ilock.udp_to_broker(topic, value):
            bclient.publish(topic, value, qos=0)
            print "From UDP: "+topic+"="+value
        else:
            print "BLOCKED from UDP: "+topic+"="+value



if __name__ == "__main__":
    bclient = broker.Client()
    bclient.on_connect = broker_on_connect
    bclient.on_message = broker_on_message

    bclient.connect(MQTT_BROKER_HOST, 1883, 60)
    print("connected", bclient)

    bl_thread = threading.Thread(target=broker_listen_thread, args=(bclient,))
    ul_thread = threading.Thread(target=udp_listen_thread, args=(bclient,))

    bl_thread.start()
    ul_thread.start()

    bl_thread.join()
    ul_thread.join()
