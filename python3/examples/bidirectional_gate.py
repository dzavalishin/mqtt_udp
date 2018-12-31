#!/bin/python

'''
Created on 24.12.2017

@author: dz

Listen to all traffic on MQTT/UDP, pump updates to MQTT broker
'''

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

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
    last = {}
    while True:
        pkt = mqttudp.sub.recv_udp_packet(s)    
        ptype,topic,value,pflags = mqttudp.sub.parse_packet(pkt)
        if ptype != "publish":
            continue
        if last.__contains__(topic) and last[topic] == value:
            continue
        last[topic] = value
        if ilock.udp_to_broker(topic, value):
            bclient.publish(topic, value, qos=0)
            print( "From UDP: "+topic+"="+value )
        else:
            print( "BLOCKED from UDP: "+topic+"="+value )



if __name__ == "__main__":
    print( "Will exchange all the traffic between MQTT/UDP and MQTT broker at "+MQTT_BROKER_HOST )

    bclient = broker.Client()
    bclient.on_connect = broker_on_connect
    bclient.on_message = broker_on_message

    bclient.connect(MQTT_BROKER_HOST, 1883, 60)
    print("connected", bclient)

    blt = threading.Thread(target=broker_listen_thread, args=(bclient,))
    ult = threading.Thread(target=udp_listen_thread, args=(bclient,))

    blt.start()
    ult.start()

    blt.join()
    ult.join()
