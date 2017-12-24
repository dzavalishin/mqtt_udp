#!/bin/python

import threading
import paho.mqtt.client as mqtt
import mqttudp.pub


udp_socket = mqttudp.pub.make_send_socket()



# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, rc, unkn):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("#")
    #client.subscribe("$SYS/#")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))

    # if you send just one message per invocation, this call is simpler
    #mqttudp.pub.send( msg.topic, msg.payload )

    # but it is better to reuse UDP socket if we publish a lot
    mqttudp.pub.send( udp_socket, msg.topic, msg.payload )



def broker_listen_thread():
        client = mqtt.Client()
        client.on_connect = on_connect
        client.on_message = on_message

#        client.connect("iot.eclipse.org", 1883, 60)
        client.connect("smart.", 1883, 60)
        print("connected", client)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
        client.loop_forever()

def writer():
    print "thread 1"

# init events
#e1 = threading.Event()
#e2 = threading.Event()

# init threads
t1 = threading.Thread(target=writer, args=())
t2 = threading.Thread(target=broker_listen_thread, args=())

# start threads
t1.start()
t2.start()

#e1.set() # initiate the first event

# join threads to the main thread
t1.join()
t2.join()



