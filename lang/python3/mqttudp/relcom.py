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
    pkt.ptype = me.PacketType.Publish
    pkt.topic = topic
    pkt.value = value
    pkt.set_qos( qos )
    pkt.pkt_id = random.randint( 1, sys.maxsize )

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
        print( "ack " + str(pkt.reply_to) + "\t\t" + str(pkt.addr) )

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
    #print(pkt.__dict__)
    #me.__send_pkt( me.make_publish_packet(pkt.topic, pkt.value, pkt.pflags ) )
    pkt.send()


def relcom_send_thread():
    while True:
        #time.sleep(0.1)
        time.sleep(1)
        __out_lock.acquire()
        for pkt in __outgoing.values():
            __do_send_publish(pkt)
            print("resend")
        __out_lock.release()


__sender = threading.Thread(target=relcom_send_thread, args=())
__sender.start()

time.clock() # must be called once to start timer on windows
random.seed()