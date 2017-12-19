#!/bin/python

import socket
import threading
import paho.mqtt.client as mqtt
import struct

# Message types
CONNECT = 0x10
CONNACK = 0x20
PUBLISH = 0x30
PUBACK = 0x40
PUBREC = 0x50
PUBREL = 0x60
PUBCOMP = 0x70
SUBSCRIBE = 0x80
SUBACK = 0x90
UNSUBSCRIBE = 0xA0
UNSUBACK = 0xB0
PINGREQ = 0xC0
PINGRESP = 0xD0
DISCONNECT = 0xE0


udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

def send_udp_packet(pkt):
    global udp_socket
    udp_socket.sendto( pkt, ("255.255.255.255", 1883) )

def pack_remaining_length(packet, remaining_length):
        remaining_bytes = []
        while True:
            byte = remaining_length % 128
            remaining_length = remaining_length // 128
            # If there are more digits to encode, set the top bit of this digit
            if remaining_length > 0:
                byte |= 0x80

            remaining_bytes.append(byte)
            packet.append(byte)
            if remaining_length == 0:
                # FIXME - this doesn't deal with incorrectly large payloads
                return packet

def pack_str16(packet, data):
        if isinstance(data, unicode):
            data = data.encode('utf-8')
        packet.extend(struct.pack("!H", len(data)))
        packet.extend(data)


def make_packet(topic, payload=b''):
    # we assume that topic and payload are already properly encoded
    assert not isinstance(topic, unicode) and not isinstance(payload, unicode) and payload is not None

    command = PUBLISH
    packet = bytearray()
    packet.append(command)

    payloadlen = len(payload)
    remaining_length = 2 + len(topic) + payloadlen

    pack_remaining_length(packet, remaining_length)
    pack_str16(packet, topic)

    packet.extend(payload)

    return packet


def send_udp_message(topic, payload=b''):
    if isinstance(topic, unicode):
        topic = topic.encode('utf-8')

    if isinstance(payload, unicode):
        payload = payload.encode('utf-8')

    pkt = make_packet(topic, payload)
    send_udp_packet(pkt)


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
    send_udp_message( msg.topic, msg.payload )



def mqtt_thread():
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
t2 = threading.Thread(target=mqtt_thread, args=())

# start threads
t1.start()
t2.start()

#e1.set() # initiate the first event

# join threads to the main thread
t1.join()
t2.join()



