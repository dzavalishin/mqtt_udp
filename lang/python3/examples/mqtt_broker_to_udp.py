#!/usr/bin/env python3

'''
	This program will subscribe to all the topics on a given
	MQTT broker and pump all the updates to MQTT/UDP environment
'''

# will work even if package is not installed 
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import threading
#import re

import paho.mqtt.client as broker

import mqttudp.engine
import mqttudp.config as cfg



cfg.setGroup('mqtt-gate')

SUBSCRIBE_TOPIC=cfg.config.get('mqtt-gate','subscribe' )
MQTT_BROKER_HOST=cfg.config.get('mqtt-gate','host' )
MQTT_BROKER_PORT=cfg.config.getint('mqtt-gate','port' )

#SUBSCRIBE_TOPIC="#"
#SUBSCRIBE_TOPIC="$SYS/#"
#MQTT_BROKER_HOST="smart."
#MQTT_BROKER_HOST="iot.eclipse.org"

#print( "'"+str(MQTT_BROKER_PORT)+"'" )

#blackList='/openhab'
#blackList=cfg.config.get('mqtt-gate','blacklist' )
blackList=cfg.get('blacklist' )


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, rc, unkn):  # @UnusedVariable
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe(SUBSCRIBE_TOPIC)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):  # @UnusedVariable
#    if (len(blackList) > 0) and (re.match( blackList, msg.topic )):
    if cfg.check_black_list(msg.topic, blackList):
        print("From broker BLACKLIST "+ msg.topic+" "+str(msg.payload))
        return

    print("From broker "+ msg.topic+" "+str(msg.payload))
    mqttudp.engine.send_publish( msg.topic, msg.payload )



def broker_listen_thread():
        client = broker.Client()
        client.on_connect = on_connect
        client.on_message = on_message

        client.connect(MQTT_BROKER_HOST, MQTT_BROKER_PORT, 60)
        print("connected", client)

        client.loop_forever()


if __name__ == "__main__":
    print( "Will resend all the traffic from MQTT broker at "+MQTT_BROKER_HOST+" to MQTT/UDP" )

    blt = threading.Thread(target=broker_listen_thread, args=())
    blt.start()
    blt.join()





