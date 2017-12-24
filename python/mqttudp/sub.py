import struct
import socket

import defs

BIND_ADDR = ("255.255.255.255",1883)

def make_recv_socket():
    udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	udp_socket.bind(BIND_ADDR)
    return udp_socket

def recv_udp_packet(udp_socket):
    data, addr = serverSock.recvfrom(2048)
    return data

def unpack_remaining_length(pkt):
	

def parse_packet(pkt):
    if pkt[0] != defs.PUBLISH:
        print "Unknown packet type"
    else:
        