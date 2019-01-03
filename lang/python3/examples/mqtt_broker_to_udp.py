#!/bin/python

'''
	This program will subscribe to all the topics on a given
	MQTT broker and pump all the updates to MQTT/UDP environment
'''

# will work even if package is not installed 
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import threading
import paho.mqtt.client as broker
import mqttudp.engine

SUBSCRIBE_TOPIC="#"
#SUBSCRIBE_TOPIC="$SYS/#"

MQTT_BROKER_HOST="smart."
#MQTT_BROKER_HOST="iot.eclipse.org"




# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, rc, unkn):  # @UnusedVariable
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe(SUBSCRIBE_TOPIC)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):  # @UnusedVariable
    global udp_socket
    print("From broker "+ msg.topic+" "+str(msg.payload))
    mqttudp.engine.send( udp_socket, msg.topic, msg.payload )



def broker_listen_thread():
        client = broker.Client()
        client.on_connect = on_connect
        client.on_message = on_message

        client.connect(MQTT_BROKER_HOST, 1883, 60)
        print("connected", client)

        client.loop_forever()


if __name__ == "__main__":
    print( "Will resend all the traffic from MQTT broker at "+MQTT_BROKER_HOST+" to MQTT/UDP" )

    global udp_socket

    udp_socket = mqttudp.engine.make_send_socket()

    blt = threading.Thread(target=broker_listen_thread, args=())
    blt.start()
    blt.join()



