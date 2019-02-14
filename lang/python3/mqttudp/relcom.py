import mqttudp.mqtt_udp_defs as defs
import mqttudp.engine as me

import threading
import time
import random
import sys

__outgoing = {}
__out_lock = threading.Lock()


def send_publish_qos( topic, value, qos ):
    global __outgoing
    pkt = me.Packet()
    pkt.topic = topic
    pkt.value = value
    pkt.set_qos( qos )
    pkt.pkt_id = random.randint( 1, sys.maxsize )

    __out_lock.acquire()
    __outgoing[ pkt.pkt_id ] = pkt
    __out_lock.release()




#
# Must be called from main program with each packet received
#
def recv_packet(pkt):
    if pkt.ptype == PacketType.PubAck:
        print( "ack " + pkt.reply_to + "\t\t" + str(addr) )

        if pkt.reply_to == 0:
            print("pkt.reply_to = 0")
            return 

        __out_lock.acquire()

        if __outgoing.__contains__( pkt.reply_to ):
            __outgoing.pop(pkt.reply_to)
            print("found")

        __out_lock.release()
        return


def __do_send_publish(pkt):
    me.__send_pkt( me.make_publish_packet(pkt.topic, pkt.value, pkt.pflags ) )


def relcom_send_thread():
    while True:
        #time.sleep(0.1)
        time.sleep(1)
        __out_lock.acquire()
        for pkt in __outgoing.values():
            __do_send_publish(pkt)
        __out_lock.release()


__sender = threading.Thread(target=relcom_send_thread, args=())
__sender.start()

time.clock() # must be called once to start timer on windows
random.seed()