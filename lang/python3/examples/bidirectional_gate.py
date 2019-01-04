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
import mqttudp.interlock
import mqttudp.config as cfg
import paho.mqtt.client as broker

SUBSCRIBE_TOPIC=cfg.config.get('mqtt-gate','subscribe' )
MQTT_BROKER_HOST=cfg.config.get('mqtt-gate','host' )
MQTT_BROKER_PORT=cfg.config.getint('mqtt-gate','port' )
#SUBSCRIBE_TOPIC="#"
#MQTT_BROKER_HOST="smart."
#MQTT_BROKER_HOST="iot.eclipse.org"



ilock = mqttudp.interlock.bidirectional(5)

def broker_on_connect(client, userdata, rc, unkn):  # @UnusedVariable
    print("Connected with result code "+str(rc))
    client.subscribe(SUBSCRIBE_TOPIC)

def broker_on_message(client, userdata, msg):  # @UnusedVariable
    #print( msg )
    if ilock.broker_to_udp(msg.topic, msg.payload):
        mqttudp.engine.send_publish_packet( msg.topic, msg.payload )
        print("To UDP: "+msg.topic+"="+str(msg.payload))
    else:
        print("BLOCKED to UDP: "+msg.topic+"="+str(msg.payload))





def broker_listen_thread(bclient):
    bclient.loop_forever()


def recv_packet_from_udp(ptype,topic,value,pflags,addr):
    global last
    if ptype != "publish":
        return
    if last.__contains__(topic) and last[topic] == value:
        return
    last[topic] = value
    if ilock.udp_to_broker(topic, value):
        bclient.publish(topic, value, qos=0)
        print( "From UDP: "+topic+"="+value )
    else:
        print( "BLOCKED from UDP: "+topic+"="+value )


def udp_listen_thread(bclient):
    global last
    last = {}
    mqttudp.engine.listen(recv_packet_from_udp)


if __name__ == "__main__":
    print( "Will exchange all the traffic between MQTT/UDP and MQTT broker at "+MQTT_BROKER_HOST )

    bclient = broker.Client()
    bclient.on_connect = broker_on_connect
    bclient.on_message = broker_on_message

    bclient.connect(MQTT_BROKER_HOST, MQTT_BROKER_PORT, 60)
    print("connected", bclient)

    blt = threading.Thread(target=broker_listen_thread, args=(bclient,))
    ult = threading.Thread(target=udp_listen_thread, args=(bclient,))

    blt.start()
    ult.start()

    blt.join()
    ult.join()
