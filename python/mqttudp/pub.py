#!/bin/python

import struct
import socket

#import defs
import mqtt_udp_defs as defs

# simplest entry point, but recreates socket every time

def send_once(topic, payload=b''):
    if isinstance(topic, unicode):
        topic = topic.encode('utf-8')

    if isinstance(payload, unicode):
        payload = payload.encode('utf-8')

    pkt = make_packet(topic, payload)
    send_udp_packet(pkt)

# simplest entry point, but recreates socket every time

def send( udp_socket, topic, payload=b''):
    if isinstance(topic, unicode):
        topic = topic.encode('utf-8')

    if isinstance(payload, unicode):
        payload = payload.encode('utf-8')

    pkt = make_packet(topic, payload)
    #udp_socket.sendto( pkt, ("255.255.255.255", 1883) )
    udp_socket.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )


def make_send_socket():
    udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    return udp_socket

def send_udp_packet(pkt):
    udp_socket = make_send_socket()
    udp_socket.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )
    udp_socket.close()


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

    command = defs.PTYPE_PUBLISH
    packet = bytearray()
    packet.append(command)

    payloadlen = len(payload)
    remaining_length = 2 + len(topic) + payloadlen

    pack_remaining_length(packet, remaining_length)
    pack_str16(packet, topic)

    packet.extend(payload)

    return packet

if __name__ == "__main__":
	import sys
	send_once(sys.argv[1], sys.argv[2])
