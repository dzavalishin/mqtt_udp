#!/bin/python

#import struct
import socket
import codecs
import struct

#import defs
import mqtt_udp_defs as defs
from array import array



# ------------------------------------------------------------------------
#
# Receive
#
# ------------------------------------------------------------------------



BIND_IP = "0.0.0.0"
#BIND_IP = socket.INADDR_ANY

def make_recv_socket():
    udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    #udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    if hasattr(udp_socket, 'SO_REUSEPORT'):  # pragma: no cover
        try:
            udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
        except udp_socket.error as err:
            print( "No SO_REUSEPORT" )
            if err[0] not in (errno.ENOPROTOOPT, errno.EINVAL):
                raise

    udp_socket.bind( (BIND_IP,defs.MQTT_PORT) )
    return udp_socket

def recv_udp_packet(udp_socket):
    data, addr = udp_socket.recvfrom(2048)
    (ip,port)=addr
    return data,ip

def unpack_remaining_length(pkt):
    remaining_length = 0
    while True:
        b = pkt[0]
        pkt = pkt[1:]
        remaining_length = remaining_length << 7
        remaining_length = remaining_length | (b & 0x7F)
        if (b & 0x80) == 0:
            break
    return remaining_length, pkt


def parse_packet(pkt):
    ptype = pkt[0] & 0xF0
    pflags = pkt[0] & 0x0F
    if ptype == defs.PTYPE_PUBLISH:

        # move up - all packets need it?
        total_len, pkt = unpack_remaining_length(pkt[1:])

        topic_len = (pkt[1] & 0xFF) | ((pkt[0] << 8) & 0xFF)   
        #topic = str( codecs.encode( str( pkt[2:topic_len+2] ), 'UTF-8' ) )
        #value = str( codecs.encode( str( pkt[topic_len+2:] ), 'UTF-8' ) )
        topic = str( pkt[2:topic_len+2], 'UTF-8' )
        value = str( pkt[topic_len+2:], 'UTF-8' )
    
        #TODO use total_len
    
        return "publish",topic,value,pflags

    if ptype == defs.PTYPE_PINGREQ:
        return "pingreq","","",pflags

    if ptype == defs.PTYPE_PINGRESP:
        return "pingresp","","",pflags

    print( "Unknown packet type" )
    #print( pkt.type() )
    for b in pkt:
        print( b )
    return "?","","",0



def listen(callback):
    """
    Will loop in caler's thread
    """
    s = make_recv_socket()
    while True:
        pkt,addr = recv_udp_packet(s)    
        ptype,topic,value,pflags = parse_packet(pkt)
        if ptype == "pingreq":
#            print( "Got ping, reply to "+addr )
            try:
                send_ping_responce(s)
            except Exception as e:
                print( "Can't send ping responce"+str(e) )
        callback(ptype,topic,value,pflags,addr)


    


# ------------------------------------------------------------------------
#
# Send 
#
# ------------------------------------------------------------------------














# simplest entry point, but recreates socket every time
'''
Kill me
'''

'''
def send_once(topic, payload=b''):
    udp_socket = make_send_socket()
    send_publish_packet( udp_socket, topic, payload ):
    udp_socket.close()
'''

'''
#    if isinstance(topic, unicode):
#        topic = topic.encode('utf-8')
    topic = topic.encode()

#    if isinstance(payload, unicode):
#        payload = payload.encode('utf-8')
    payload = payload.encode()

    pkt = make_packet(topic, payload)
    send_udp_packet(pkt)
'''
# simplest entry point, but recreates socket every time

def send_publish_packet( udp_socket, topic, payload=b''):
    if isinstance(topic, str):
	    topic = topic.encode()

    if isinstance(payload, str):
	    payload = payload.encode()

    pkt = make_packet(topic, payload)
    udp_socket.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )


def make_send_socket():
    udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    return udp_socket


'''
def send_udp_packet(pkt):
    udp_socket = make_send_socket()
    udp_socket.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )
    udp_socket.close()
'''

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
#        if isinstance(data, unicode):
#            data = data.encode('utf-8')
#        data = data.encode()
        packet.extend(struct.pack("!H", len(data)))
        packet.extend(data)


def make_packet(topic, payload=b''):
    # we assume that topic and payload are already properly encoded
#    assert not isinstance(topic, unicode) and not isinstance(payload, unicode) and payload is not None

    command = defs.PTYPE_PUBLISH
    packet = bytearray()
    packet.append(command)

    payloadlen = len(payload)
    remaining_length = 2 + len(topic) + payloadlen

    pack_remaining_length(packet, remaining_length)
    pack_str16(packet, topic)

    packet.extend(payload)

    return packet

#
# Ping support
#

def make_ping_packet():
    command = defs.PTYPE_PINGREQ
    packet = bytearray()
    packet.append(command)
    pack_remaining_length(packet, 0)
    return packet


def send_ping(udp_socket):
    pkt = make_ping_packet()
    udp_socket.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )

def make_ping_responce_packet():
    command = defs.PTYPE_PINGRESP
    packet = bytearray()
    packet.append(command)
    pack_remaining_length(packet, 0)
    return packet

'''
def send_ping_responce(udp_socket,addr):
    pkt = make_ping_responce_packet()
    udp_socket.sendto( pkt, (addr, defs.MQTT_PORT) )
'''

def send_ping_responce(udp_socket):
    pkt = make_ping_responce_packet()
    udp_socket.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )


#if __name__ == "__main__":
#	import sys
#	send_once(sys.argv[1], sys.argv[2])

