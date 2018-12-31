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
import mqttudp.pub

SUBSCRIBE_TOPIC="#"
#SUBSCRIBE_TOPIC="$SYS/#"

MQTT_BROKER_HOST="smart."
#MQTT_BROKER_HOST="iot.eclipse.org"

udp_socket = mqttudp.pub.make_send_socket()



# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, rc, unkn):  # @UnusedVariable
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe(SUBSCRIBE_TOPIC)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):  # @UnusedVariable
    print(msg.topic+" "+str(msg.payload))

    # if you send just one message per invocation, this call is simpler
    #mqttudp.pub.send( msg.topic, msg.payload )

    # but it is better to reuse UDP socket if we publish a lot
    mqttudp.pub.send( udp_socket, msg.topic, msg.payload )



def broker_listen_thread():
        client = broker.Client()
        client.on_connect = on_connect
        client.on_message = on_message

        client.connect(MQTT_BROKER_HOST, 1883, 60)
        print("connected", client)

        client.loop_forever()


if __name__ == "__main__":
    print( "Will resend all the traffic from MQTT broker at "+MQTT_BROKER_HOST+" to MQTT/UDP" )
    blt = threading.Thread(target=broker_listen_thread, args=())
    blt.start()
    blt.join()



