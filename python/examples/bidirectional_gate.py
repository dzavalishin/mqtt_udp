'''
Created on 24.12.2017

@author: dz

Listen to all traffic on MQTT/UDP, pump updates to MQTT broker
'''

# will work even if package is not installed 
import sys
sys.path.append('..')

import threading
import mqttudp.sub
import paho.mqtt.client as broker

SUBSCRIBE_TOPIC="#"

MQTT_BROKER_HOST="smart."
#MQTT_BROKER_HOST="iot.eclipse.org"


udp_send_socket = mqttudp.pub.make_send_socket()

ilock = mqttudp.interlock.bidirectional(5)

def on_connect(client, userdata, rc, unkn):  # @UnusedVariable
    print("Connected with result code "+str(rc))
    client.subscribe(SUBSCRIBE_TOPIC)

def on_message(client, userdata, msg):  # @UnusedVariable
    print(msg.topic+" "+str(msg.payload))
    if ilock.broker_to_udp(msg.topic, msg.value):
        mqttudp.pub.send( udp_send_socket, msg.topic, msg.payload )





def broker_listen_thread(bclient):
    bclient.loop_forever()

def udp_listen_thread(bclient):
    s = mqttudp.sub.make_recv_socket()
    last = {}
    while True:
        pkt = mqttudp.sub.recv_udp_packet(s)    
        topic,value = mqttudp.sub.parse_packet(pkt)
        if last.has_key(topic) and last[topic] == value:
            continue
        last[topic] = value
        print topic+"="+value
        if ilock.udp_to_broker(topic, value):
            bclient.publish(topic, value, qos=0)



if __name__ == "__main__":
    bclient = broker.Client()
    #client.on_connect = on_connect
    #client.on_message = on_message

    bclient.connect(MQTT_BROKER_HOST, 1883, 60)
    print("connected", bclient)

    blt = threading.Thread(target=broker_listen_thread, args=(bclient))
    ult = threading.Thread(target=udp_listen_thread, args=(bclient))

    blt.start()
    ult.start()

    blt.join()
    ult.join()
    
