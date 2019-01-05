#!/bin/python

#import struct
import socket

#import defs
import mqtt_udp_defs as defs
#from array import array

BIND_IP = "0.0.0.0"
#BIND_IP = socket.INADDR_ANY

def make_recv_socket():
    udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    #udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    udp_socket.bind( (BIND_IP,defs.MQTT_PORT) )
    return udp_socket

def recv_udp_packet(udp_socket):
    data, addr = udp_socket.recvfrom(2048)
    return data

def unpack_remaining_length(pkt):
    remaining_length = 0
    while True:
        b = ord(pkt[0])
        pkt = pkt[1:]
        remaining_length = remaining_length << 7
        remaining_length = remaining_length | (b & 0x7F)
        if (b & 0x80) == 0:
            break
    return remaining_length, pkt


def parse_packet(pkt):
    if ord(pkt[0]) == defs.PTYPE_PUBLISH:
        
        total_len, pkt = unpack_remaining_length(pkt[1:])

        topic_len = (ord(pkt[1]) & 0xFF) | ((ord(pkt[0]) << 8) & 0xFF)   
        topic = pkt[2:topic_len+2].encode('UTF-8')    
        value = pkt[topic_len+2:].encode('UTF-8')
    
        #TODO use total_len
    
        return "publish",topic,value

    if ord(pkt[0]) == defs.PTYPE_PINGREQ:
        return "pingreq","",""

    if ord(pkt[0]) == defs.PTYPE_PINGRESP:
        return "pingresp","",""

    print( "Unknown packet type" )
    #print( pkt.type() )
    for b in pkt:
        print ord(b)
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
            print topic+"="+value
        if ptype == "pingreq":
            print ptype
        if ptype == "pingresp":
            print ptype




