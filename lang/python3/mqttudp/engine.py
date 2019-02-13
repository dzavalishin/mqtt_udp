#!/bin/python

import socket
import time
import datetime
import struct
from enum import Enum

import hashlib
import hmac

import mqttudp.mqtt_udp_defs as defs
#from array import array


# ------------------------------------------------------------------------
#
# Packet class
#
# ------------------------------------------------------------------------

class PacketType(Enum):
    Unknown     = 0
    Publish     = 0x30
    Subscribe   = 0x80
    PingReq     = 0xC0
    PingResp    = 0xD0


class Packet(object):
    def __init__( self, ptype, topic, value, pflags, ttrs ):
        self.ptype  = ptype
        self.pflags = pflags
        self.topic  = topic
        self.value  = value
        self.ttrs   = ttrs
        self.addr   = None
        self.signed = False
        self.pkt_id = 0
        self.__signature = None
        self.__signature_start = 0

    def __init__( self ):
        self.ptype  = None
        self.pflags = 0
        self.topic  = ""
        self.value  = ""
        self.ttrs   = None
        self.addr   = None
        self.signed = False
        self.pkt_id = 0
        self.__signature = None
        self.__signature_start = 0



# ------------------------------------------------------------------------
#
# Globals
#
# ------------------------------------------------------------------------

__SEND_SOCKET = None



# ------------------------------------------------------------------------
#
# Getters/Setters
#
# ------------------------------------------------------------------------

muted = False

def set_muted(mode: bool):
    global muted
    muted = mode



# up to 3 packets can be sent with no throttle
# most devices have some buffer and we do not
# want to calc time each send

throttle = 100
max_seq_packets = 3 

# set to 0 to turn throttling off

def set_throttle(msec: int):
    global throttle
    throttle = msec


class ErrorType(Enum):
    Unexpected  = 0
    IO          = 1
    Timeout     = 2
    Protocol    = 3
    Invalid     = 4    # Invalid parameter


user_error_handler = None
#
# function( retcode : int, etype : ErrorType, msg : str )
#
def set_error_handler( error_handler ):
    global user_error_handler
    user_error_handler = error_handler

def error_handler( retcode : int, etype : ErrorType, msg : str ):
    if user_error_handler != None :
        return user_error_handler( retcode, etype, msg )

    print("MQTT/UDP Error "+str(etype)+" rc="+str(retcode)+" "+msg)
    return retcode

# ------------------------------------------------------------------------
#
# Signature
#
# ------------------------------------------------------------------------
#import importlib

__signature_key = None

def set_signature( key ):
    global __signature_key
    if type(key) == str:
        key = key.encode('utf-8')
    __signature_key = key
    #print(str(type(__signature_key)))

def sign_data( msg ):
    if type(msg) == str:
        msg=msg.encode('utf-8')
    out = hmac.new( __signature_key, msg, digestmod=hashlib.md5 ).hexdigest()
    # hmac.digest(key, msg, digest)¶
    return bytearray.fromhex( out )

def sign_and_ttr( msg ):
    if type(msg) == str:
        msg=msg.encode('utf-8')
    signature = sign_data( msg )
    out = bytearray()
    out += msg
    out += b's'
    len = 16
    out.append(len)
    out += signature
    return out


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
            #print( "No SO_REUSEPORT" )
            error_handler( err[0], ErrorType.IO, "No SO_REUSEPORT" )
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
    eaten = 0;
    while True:
        b = pkt[0]
        eaten += 1
        pkt = pkt[1:]
        remaining_length = remaining_length << 7
        remaining_length = remaining_length | (b & 0x7F)
        if (b & 0x80) == 0:
            break
    return remaining_length, pkt, eaten


def parse_ttr( tag, value, pobj, start_pos ):
    #print("TTR tag="+str(tag))
    #print("TTR val="+str(value))
    #print("TTR type="+str(type(value)))
    #print("TTR len="+str(len(value)))
        
        
    if tag == 110:
        num,  = struct.unpack("!I", value)
        me = ( "PacketNumber", num ) # kill
        pobj.pkt_id = num
    elif tag == 115:
        #num,  = struct.unpack("I", value)
        me = ( "MD5", value.hex() )
        pobj.__signature = value
        pobj.__signature_start = start_pos
        print("Sig found")
    else:
        me =(str(tag),value)
    return (me,)


def parse_ttrs(pktrest, pobj, start_pos ):
    ttr_tag = pktrest[0]
    ttr_len = pktrest[1] # TODO decode len!

    #print("TTRs len "+str(len(pktrest)))
    #print("TTR len "+str(ttr_len))

    if ttr_len & 0x80:
        return error_handler( -1, ErrorType.Protocol, "TTR len > 0x7F: "+str(ttr_len) )
    
    ttr = parse_ttr( ttr_tag, pktrest[2:ttr_len+2], pobj, start_pos )

    #(ttr_type, ttr_value) = ttr
    #if ttr_type == 's':
    #    printf("Got signature TTR")

    if len(pktrest) > ttr_len+2:
        ttrs = parse_ttrs(pktrest[ttr_len+2:], pobj, start_pos+ttr_len+2 )
    else:
        ttrs = ()
    
    ttrs += ttr
    
    return ttrs 

def parse_packet(pkt):
    full_pkt = pkt # for digital signature
    out = Packet()

    ptype = pkt[0] & 0xF0
    out.pflags = pkt[0] & 0x0F
    total_len, pkt, eaten = unpack_remaining_length(pkt[1:])
    
    ttrs = None
    if len(pkt) > total_len:
        #print("have TTRs @ 1 + "+str(eaten)+" + "+str(total_len))
        out.ttrs = parse_ttrs( pkt[total_len:], out, 1+eaten+total_len )
        # TODO kill out.ttrs
        #print(ttrs)

        if (out.__signature != None) and (__signature_key != None):
            #print("check 0:"+str(out.__signature_start))
            us_signature = sign_data( full_pkt[0:out.__signature_start] )
            if us_signature == out.__signature:
                out.is_signed = True
                print("Signed OK!")
            else:
                error_handler( -1, ErrorType.Protocol, "Packet signature is wrong" )
                #print( full_pkt.hex() )
    
    if ptype == defs.PTYPE_PUBLISH:
        # move up - all packets need it?
        topic_len = (pkt[1] & 0xFF) | ((pkt[0] << 8) & 0xFF)   
        out.topic = str( pkt[2:topic_len+2], 'UTF-8' )
        out.value = str( pkt[topic_len+2:total_len], 'UTF-8' )
        out.ptype = PacketType.Publish
        return out

    if ptype == defs.PTYPE_SUBSCRIBE:
        # move up - all packets need it?
        topic_len = (pkt[1] & 0xFF) | ((pkt[0] << 8) & 0xFF)   
        out.topic = str( pkt[2:topic_len+2], 'UTF-8' )
        #TODO use total_len
    
        out.ptype = PacketType.Subscribe
        return out


    if ptype == defs.PTYPE_PINGREQ:
        out.ptype = PacketType.PingReq
        return out

    if ptype == defs.PTYPE_PINGRESP:
        out.ptype = PacketType.PingResp
        return out

    #print( "Unknown packet type" )
    error_handler( ptype, ErrorType.Protocol, "Unknown packet type" )
    #print( pkt.type() )
    #for b in pkt:
    #    print( b )
    #return "?","","",0
    put.ptype = PacketType.Unknown
    return out



def listen(callback):
    """
    Will loop in caler's thread
    """
    s = make_recv_socket()
    while True:
        pkt,addr = recv_udp_packet(s)    
        pobj = parse_packet(pkt)
        pobj.addr = addr
        if (not muted) and (pobj.ptype == PacketType.PingReq):
#            print( "Got ping, reply to "+addr )
            try:
                send_ping_responce()
            except Exception as e:
                #print( "Can't send ping responce"+str(e) )
                error_handler( -1, ErrorType.IO, "Can't send ping responce"+str(e) )
        callback(pobj)


    


# ------------------------------------------------------------------------
#
# Send 
#
# ------------------------------------------------------------------------



def __send_pkt( pkt ):
    if __signature_key != None:
        pkt = sign_and_ttr( pkt )
    throttle_me()
    __SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )




def send_publish( topic, payload=b''):
    if isinstance(topic, str):
	    topic = topic.encode()

    if isinstance(payload, str):
	    payload = payload.encode()

    pkt = make_publish_packet(topic, payload)
    #throttle_me()
    #__SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )
    __send_pkt( pkt )


def __make_send_socket():
    udp_socket = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    return udp_socket



def pack_remaining_length(packet, remain_length):
        remain_bytes = []
        while True:
            byte = remain_length % 128
            remain_length = remain_length // 128
            # If there are more digits to encode, set the top bit of this digit
            if remain_length > 0:
                byte |= 0x80

            remain_bytes.append(byte)
            packet.append(byte)
            if remain_length == 0:
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
    if isinstance(topic, str):
        topic = topic.encode()
    pkt = make_subscribe_packet(topic)
    #throttle_me()
    #__SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )
    __send_pkt( pkt )

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
    #throttle_me()
    #__SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )
    __send_pkt( pkt )



def make_ping_responce_packet():
    command = defs.PTYPE_PINGRESP
    packet = bytearray()
    packet.append(command)
    pack_remaining_length(packet, 0)
    return packet

def send_ping_responce():
    pkt = make_ping_responce_packet()
    #throttle_me()
    #__SEND_SOCKET.sendto( pkt, ("255.255.255.255", defs.MQTT_PORT) )
    __send_pkt( pkt )







# ------------------------------------------------------------------------
#
# Send throttle
#
# Will keep send pace. Up to {max_seq_packets} will be passed with no pause,
# but more than that will be paused so that average time between packets
# will be about {throttle} msec.
#
# ------------------------------------------------------------------------


def time_msec():
    return round(datetime.datetime.utcnow().timestamp() * 1000)

last_send_time = 0
last_send_count = 0


def throttle_me():
    global last_send_count, last_send_time

    if throttle == 0:
        return;

    last_send_count += 1
    if last_send_count < max_seq_packets:
        return

    last_send_count = 0;

    now = time_msec()
    #print( str(now) )
    since_last_pkt = now - last_send_time

    if last_send_time == 0:
        last_send_time = now
        return

    last_send_time = now

    towait = max_seq_packets * throttle - since_last_pkt

    #print( str(towait) )

    if towait <= 0:
        return

    time.sleep( 0.001 * towait )

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
