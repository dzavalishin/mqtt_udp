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
import mqttudp.engine
import paho.mqtt.client as broker

MQTT_BROKER_HOST="smart."
#MQTT_BROKER_HOST="iot.eclipse.org"


def broker_listen_thread(bclient):
    bclient.loop_forever()


def recv_packet_from_udp(ptype,topic,value,pflags,addr):
    global last
    if ptype != "publish":
        return
    if last.__contains__(topic) and last[topic] == value:
        return
    last[topic] = value
    print( topic+"="+value )
    bclient.publish(topic, value, qos=0)


if __name__ == "__main__":
    print( "Will resend all the MQTT/UDP traffic to MQTT broker at "+MQTT_BROKER_HOST )

    global last
    last = {}

    bclient = broker.Client()
    #client.on_connect = on_connect
    #client.on_message = on_message

    bclient.connect(MQTT_BROKER_HOST, 1883, 60)
    print("connected", bclient)

    blt = threading.Thread(target=broker_listen_thread, args=(bclient,))
    blt.start()

    mqttudp.engine.listen(recv_packet_from_udp)
    blt.join()

'''
    s = mqttudp.engine.make_recv_socket()
    last = {}
    while True:
        pkt = mqttudp.engine.recv_udp_packet(s)    
        ptype,topic,value,pflags = mqttudp.engine.parse_packet(pkt)
        if ptype != "publish":
            continue
        if last.__contains__(topic) and last[topic] == value:
            continue
        last[topic] = value
        print( topic+"="+value )
        bclient.publish(topic, value, qos=0)
    blt.join()
'''
 
