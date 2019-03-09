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
    PubAck      = defs.PTYPE_PUBACK
    Subscribe   = 0x80
    PingReq     = 0xC0
    PingResp    = 0xD0


class Packet(object):
    #def __init__( self, ptype, topic, value, pflags, ttrs ):
    #    self.ptype  = ptype
    #    self.pflags = pflags
    #    self.topic  = topic
    #    self.value  = value
    #    self.ttrs   = ttrs
    #    self.addr   = None
    #    self.signed = False
    #    self.pkt_id = 0
    #    self.reply_to = 0
    #    self.private_signature = None
    #    self.private_signature_start = 0
    #    self.ack_count = 0 # Used in ack processing, see PubAck, etc - count of ack packets we got for this sent one
    #    self.send_count = 0 # how many times it was (re)sent

    def __init__( self ):
        self.ptype  = None
        self.pflags = 0
        self.topic  = ""
        self.value  = ""
        self.ttrs   = None
        self.addr   = None
        self.signed = False
        self.pkt_id = 0
        self.reply_to = 0
        self.private_signature = None
        self.private_signature_start = 0
        self.ack_count = 0 # Used in ack processing, see PubAck, etc
        self.send_count = 0 # how many times it was (re)sent
    
    def get_qos(self):
        return (self.pflags >> 1) & 0x3

    def set_qos(self, qos):
        self.pflags &= 0x6
        self.pflags |= (qos & 0x3) << 1

    def send(self):
        self.send_count += 1

        topic = self.topic
        if isinstance(topic, str):
	        topic = topic.encode()
        payload = self.value
        if isinstance(payload, str):
	        payload = payload.encode()

        command = self.ptype.value
        packet = bytearray()
        packet.append(command | (self.pflags & 0xF) )

        #print( str(packet))

        if self.ptype == PacketType.Publish:
            payloadlen = len(payload)
        else:
            payloadlen = 0

        if (self.ptype == PacketType.Publish) or (self.ptype == PacketType.Subscribe):
            topiclen = 2 + len(topic)
        else:
            topiclen = 0

        remaining_length = topiclen + payloadlen
        pack_remaining_length(packet, remaining_length)

        if (self.ptype == PacketType.Publish) or (self.ptype == PacketType.Subscribe):
            pack_str16(packet, topic)

        if self.ptype == PacketType.Publish:
            packet.extend(payload)

        print("send id="+str(self.pkt_id))
        packet = add_integer_ttr( packet, b'n', self.pkt_id )
        
        if self.reply_to != 0:
            packet = add_integer_ttr( packet, b'r', self.reply_to )

        private_send_pkt( packet )




# ------------------------------------------------------------------------
#
# Globals
#
# ------------------------------------------------------------------------

__SEND_SOCKET = None
__BROADCAST_ADDR = "255.255.255.255"
__BIND_ADDR = "0.0.0.0"
#__BIND_ADDR = socket.INADDR_ANY


# ------------------------------------------------------------------------
#
# Getters/Setters
#
# ------------------------------------------------------------------------

muted = False

def set_muted(mode: bool):
    global muted
    muted = mode


# Set address to be used as broadcast.
# You need to call this to define which network
# interface to use, if your computer has more 
# than one

def set_broadcast_address( baddr ):
    global __BROADCAST_ADDR
    __BROADCAST_ADDR = baddr


# Set address to use for reception (bind() system call)

def set_bind_address( baddr ):
    global __BIND_ADDR
    __BIND_ADDR = baddr

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
# Add TTR
#
# ------------------------------------------------------------------------

def __add_ttr( pkt, ttr_key, ttr_data ):
    out = bytearray()
    out += pkt
    out += ttr_key
    dlen = len(ttr_data)
    out.append(dlen)
    out += ttr_data
    return out

# ttr_value : int
def add_integer_ttr( pkt, ttr_key, ttr_value ):
    ttr_data = struct.pack("!I", ttr_value)
    return __add_ttr( pkt, ttr_key, ttr_data )

# ------------------------------------------------------------------------
#
# Signature
#
# ------------------------------------------------------------------------
#import importlib

__signature_key = None

def set_signature( key ):
    global __signature_key
    if isinstance(key, str):
        key = key.encode('utf-8')
    __signature_key = key
    #print(str(type(__signature_key)))

def sign_data( msg ):
    if isinstance(msg, str):
        msg=msg.encode('utf-8')
    out = hmac.new( __signature_key, msg, digestmod=hashlib.md5 ).hexdigest()
    # hmac.digest(key, msg, digest)¶
    return bytearray.fromhex( out )

def sign_and_ttr( msg, ttrs = None ):
    if isinstance(msg, str):
        msg=msg.encode('utf-8')
    signature = sign_data( msg ) # TODO use __add_ttr( pkt, ttr_key, ttr_data )
    out = bytearray()
    out += msg
    out += b's'
    tlen = 16
    out.append(tlen)
    out += signature
    return out


# ------------------------------------------------------------------------
#
# Receive
#
# ------------------------------------------------------------------------


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

    udp_socket.bind( (__BIND_ADDR,defs.MQTT_PORT) )
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
        
        
    if tag == 110: # n
        num,  = struct.unpack("!I", value)
        me = ( "PacketNumber", num ) # kill
        pobj.pkt_id = num
    elif tag == 114: # r
        num,  = struct.unpack("!I", value)
        me = ( "ReplyTo", num ) # kill
        pobj.reply_to = num
    elif tag == 115: # s
        me = ( "MD5", value.hex() ) # kill
        pobj.private_signature = value
        pobj.private_signature_start = start_pos
        #print("Sig found")
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
    #print( out.__dict__ )

    ptype = pkt[0] & 0xF0
    out.pflags = pkt[0] & 0x0F
    total_len, pkt, eaten = unpack_remaining_length(pkt[1:])
    
    ttrs = None
    if len(pkt) > total_len:
        #print("have TTRs @ 1 + "+str(eaten)+" + "+str(total_len))
        out.ttrs = parse_ttrs( pkt[total_len:], out, 1+eaten+total_len )
        # TODO kill out.ttrs
        #print(ttrs)
        #print( "\n-------------------\n" )
        #print( out.__dict__ )
        #print( "\n-------------------\n" )
        if (out.private_signature != None) and (__signature_key != None):
            #print("check 0:"+str(out.private_signature_start))
            us_signature = sign_data( full_pkt[0:out.private_signature_start] )
            if us_signature == out.private_signature:
                out.signed = True
                #print("Signed OK!")
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

    if ptype == defs.PTYPE_PUBACK:
        out.ptype = PacketType.PubAck
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
    out.ptype = PacketType.Unknown
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
        if not muted:
            if pobj.ptype == PacketType.PingReq:
#                print( "Got ping, reply to "+addr )
                try:
                    send_ping_responce()
                except Exception as e:
                    #print( "Can't send ping responce"+str(e) )
                    error_handler( -1, ErrorType.IO, "Can't send ping responce"+str(e) )

            # TODO don't reply to own packets
            # TODO packet id is not enough, need "repy to sender host id" in PubAck too
            if (pobj.reply_to != 0) and (pobj.ptype == PacketType.Publish):
                qos = pobj.get_qos()
                if qos > max_qos:
                    qos = max_qos
                if not is_packet_from_us( pobj ):
                    send_puback( pobj.reply_to, qos )
            

        callback(pobj)

relcom_is_packet_from_us_callback = None

def is_packet_from_us( pobj ):
    if relcom_is_packet_from_us_callback != None:
        return relcom_is_packet_from_us_callback

def set_relcom_is_packet_from_us_callback( cb ):
    relcom_is_packet_from_us_callback = cb

# ------------------------------------------------------------------------
#
# Send 
#
# ------------------------------------------------------------------------


def private_send_pkt( pkt, ttrs = None ):
    # TODO __add_ttr( pkt, ttr_key, ttr_data )
    if __signature_key != None:
        pkt = sign_and_ttr( pkt, ttrs )
    throttle_me()
    __SEND_SOCKET.sendto( pkt, (__BROADCAST_ADDR, defs.MQTT_PORT) )




def send_publish( topic, payload=b''):
    pkt = make_publish_packet(topic, payload)
    private_send_pkt( pkt )


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


def make_publish_packet(topic, payload=b'', flags = 0):
    if isinstance(topic, str):
	    topic = topic.encode()

    if isinstance(payload, str):
	    payload = payload.encode()

    #print(qos)

    command = defs.PTYPE_PUBLISH
    #flags = (qos & 0x3) << 1
    packet = bytearray()
    packet.append(command | (flags & 0xF) )

    #print( str(packet))

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

    packet.append(0) # QoS byte - not needed, we do not use it

    return packet

def send_subscribe(topic):
    if isinstance(topic, str):
        topic = topic.encode()
    pkt = make_subscribe_packet(topic)
    private_send_pkt( pkt )


#
# Make packet with no content
#

def __make_simple_packet(ptype):
    packet = bytearray()
    packet.append(ptype)
    pack_remaining_length(packet, 0)
    return packet


#
# Ping support
#

def send_ping():
    private_send_pkt( __make_simple_packet(defs.PTYPE_PINGREQ) )


def send_ping_responce():
    private_send_pkt( __make_simple_packet(defs.PTYPE_PINGRESP) )


#
# PubAck support
#


def send_puback(reply_to, qos):
    pkt = __make_simple_packet(defs.PTYPE_PUBACK or ((qos and 0x3) << 1))
    pkt = add_integer_ttr( pkt, b'r', reply_to )
    private_send_pkt( pkt )






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
