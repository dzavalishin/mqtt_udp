#!/bin/python

import socket
#import codecs
import struct

import mqtt_udp_defs as defs
#from array import array


# ------------------------------------------------------------------------
#
# Globals
#
# ------------------------------------------------------------------------

__SEND_SOCKET = None
#__SEND_SOCKET = __make_send_socket()

#def init():
#    __SEND_SOCKET = __make_send_socket()



# ------------------------------------------------------------------------
#
# Getters/Setters
#
# ------------------------------------------------------------------------

muted = False

def set_muted(mode: bool):
    global muted
    muted = mode

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
    #(ip,port)=addr
    (ip,_)=addr
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
        if (not muted) and (ptype == "pingreq"):
#            print( "Got ping, reply to "+addr )
            try:
                send_ping_responce()
            except Exception as e:
                print( "Can't send ping responce"+str(e) )
        callback(ptype,topic,value,pflags,addr)


    


# ------------------------------------------------------------------------
#
# Send 
#
# ------------------------------------------------------------------------








def send_publish( topic, payload=b''):
    if isinstance(topic, str):
	    topic = topic.encode()

    if isinstance(payload, str):
	    payload = payload.encode()

    pkt = make_publish_packet(topic, payload)
    __SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )


def __make_send_socket():
    udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    return udp_socket



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


def make_publish_packet(topic, payload=b''):
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
#
#

def make_subscribe_packet(topic):
    command = defs.PTYPE_SUBSCRIBE
    packet = bytearray()
    packet.append(command)

    remaining_length = 2 + len(topic) + 1

    pack_remaining_length(packet, remaining_length)
    pack_str16(packet, topic)

    packet.append(0) # QoS byte

    return packet

def send_subscribe(topic):
    pkt = make_subscribe_packet(topic)
    __SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )


#
# Ping support
#

def make_ping_packet():
    command = defs.PTYPE_PINGREQ
    packet = bytearray()
    packet.append(command)
    pack_remaining_length(packet, 0)
    return packet

def send_ping():
    pkt = make_ping_packet()
    __SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )



def make_ping_responce_packet():
    command = defs.PTYPE_PINGRESP
    packet = bytearray()
    packet.append(command)
    pack_remaining_length(packet, 0)
    return packet

def send_ping_responce():
    pkt = make_ping_responce_packet()
    __SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )



# ------------------------------------------------------------------------
#
# Topic match
#
# ------------------------------------------------------------------------


def match( tfilter, topicName ):
		
    tc = 0;
    fc = 0;
    
    tlen = len( topicName )
    flen = len( tfilter )
    
    while True:
    
        # begin of path part
        
        if tfilter[fc] == '+':
    
            fc+= 1; # eat +
            # matches one path part, skip all up to / or end in topic
            while (tc < tlen) and (topicName[tc] != '/'):
                tc+= 1; # eat all non slash
            
            # now either both have /, or both at end
            
            # both finished
            if (tc == tlen) and ( fc == flen ):
                return True;
    
            # one finished, other not
            if (tc == tlen) != ( fc == flen ):
                return False;
            
            # both continue
            if (topicName[tc] == '/') and (tfilter[fc] == '/'):
                tc+= 1;
                fc+= 1;
                continue; # path part eaten
    
            # one of them is not '/' ?
            return False;
    
        
        # TODO check it to be at end?
        # we came to # in tfilter, done
        if tfilter[fc] == '#':
            return True
    
        # check parts to be equal
        while True:
    
            # both finished
            if (tc == tlen) and ( fc == flen ):
                return True;
    
            # one finished
            if (tc == tlen) or ( fc == flen ):
                return False;
    
            # both continue
            if (topicName[tc] == '/') and (tfilter[fc] == '/'):
                tc+= 1;
                fc+= 1;
                break; # path part eaten

            # both continue
    
            if topicName[tc] != tfilter[fc]:
                return False;

            # continue
            tc+= 1;
            fc+= 1;
    
    
        






# ------------------------------------------------------------------------
#
# Init
#
# ------------------------------------------------------------------------


__SEND_SOCKET = __make_send_socket()


# ------------------------------------------------------------------------
#
# Main for impatient ones
#
# ------------------------------------------------------------------------


if __name__ == "__main__":
	import sys
	send_publish(sys.argv[1], sys.argv[2])
