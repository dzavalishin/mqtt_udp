#!/bin/python

#import struct
import socket
import codecs

#import defs
import mqtt_udp_defs as defs
from array import array

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
    return data

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
    return

    
if __name__ == "__main__":
    s = make_recv_socket()
    last = {}
    while True:
        pkt = recv_udp_packet(s)    
        ptype,topic,value = parse_packet(pkt)
        #if last.has_key(topic) and last[topic] == value:
        #    continue
        if ptype == "publish":
            last[topic] = value
            print( topic+"="+value )
        if ptype == "pingreq":
            print( ptype )
        if ptype == "pingresp":
            print( ptype )




