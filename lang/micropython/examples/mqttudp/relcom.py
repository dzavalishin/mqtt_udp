import mqttudp.mqtt_udp_defs as defs
import mqttudp.engine as me

import threading
import time
import random
import sys

__outgoing = {}
__out_lock = threading.Lock()

MAX_RESEND_COUNT = 3
MIN_LOW_QOS_ACK = 2 # acks with -1 QoS

def send_publish_qos( topic, value, qos ):
    global __outgoing
    pkt = me.Packet()
    pkt.ptype = me.PacketType.Publish
    pkt.topic = topic
    pkt.value = value
    pkt.set_qos( qos )
    #pkt.pkt_id = 0xFFFFFFFF & random.randint( 1, sys.maxsize )
    pkt.pkt_id = 0x00000FFF & random.randint( 1, sys.maxsize )
    #pkt.pkt_id = 33

    #print(pkt.__dict__)

    __out_lock.acquire()

    old_id = None

    # kill older ones with same topic
    for old in __outgoing.values():
        if old.topic == topic:
            old_id = old.pkt_id
            break

    if old_id != None:
        __outgoing.pop(old_id)

    __outgoing[ pkt.pkt_id ] = pkt
    __out_lock.release()




#
# Must be called from main program with each packet received
#
def recv_packet(pkt):
    if pkt.ptype == me.PacketType.PubAck:
        #print( "ack " + str(pkt.reply_to) + "\t\t" + str(pkt.addr) )
        print( "ack " + str(pkt.reply_to) + " QoS " + str(pkt.get_qos()) )

        if pkt.reply_to == 0:
            print("pkt.reply_to = 0")
            return 

        #print(__outgoing)

        __out_lock.acquire()

        if __outgoing.__contains__( pkt.reply_to ):
            sent = __outgoing.get(pkt.reply_to)
            if sent.get_qos() == pkt.get_qos():
                __outgoing.pop(pkt.reply_to)
                print("found same QoS, kill")
            elif sent.get_qos() == pkt.get_qos() + 1:
                print("found -1 QoS, increment")
                sent.ack_count += 1
                if sent.ack_count >= MIN_LOW_QOS_ACK:
                    print("-1 QoS ack count ok, kill")
                    __outgoing.pop(pkt.reply_to)

        __out_lock.release()
        return


def __do_send_publish(pkt):
    #print(pkt.__dict__)
    #me.__send_pkt( me.make_publish_packet(pkt.topic, pkt.value, pkt.pflags ) )
    pkt.send()

# TODO does not work :(
def is_packet_from_us( pobj ):
    # TODO check if this packet is sent by us
    if __outgoing.__contains__( pobj.pkt_id ):
        print("Our pkt, skip ACK")
        return True
    return False

me.set_relcom_is_packet_from_us_callback( is_packet_from_us )

def relcom_send_thread():
    while True:
        #time.sleep(0.1)
        time.sleep(0.3)
        #time.sleep(1)
        __out_lock.acquire()
        
        removed_some = True # to enter while
        while removed_some:
            removed_some = False
            for pkt in __outgoing.values():
                if pkt.send_count > MAX_RESEND_COUNT:
                    __outgoing.pop(pkt.pkt_id)
                    print("too many resends, kill "+str(pkt.pkt_id))
                    removed_some = True
                    break

        for pkt in __outgoing.values():
            __do_send_publish(pkt)
            print("resend")
        __out_lock.release()


__sender = threading.Thread(target=relcom_send_thread, args=())
__sender.start()

time.clock() # must be called once to start timer on windows
random.seed()