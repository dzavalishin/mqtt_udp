#!/usr/bin/env python3

'''
@author: dz

Listen to one topic on MQTT/UDP, if topic value is
out of bounds, send message to another topic.

If used together with tools/tray program, message will
be displayed in system tray of corresponding computer
or computers.

'''
# will work even if package is not installed
import sys
sys.path.append('..')

import time

import mqttudp.engine

# On my system it's mains voltage
LISTEN_TOPIC="PLK0_Va"

# Will trigger message if value is above
VALUE_LIMIT=218

# System tray applicattion listens to it
TRIGGER_TOPIC="tray/message"

# Snooze time, seconds
SNOOZE = 30



start_time = time.time() - SNOOZE - 1; # first one must trigger


def recv_packet(ptype,topic,value,pflags,addr):
    global start_time

    if ptype != "publish":
        return

    if topic != LISTEN_TOPIC:
        return

    #print( "Got " + value )

    # can't convert - ignore
    try:
        iv = float( value )
    except ValueError:
        return


    if iv < VALUE_LIMIT:
        return

    now = time.time();
    #print( "\t\t time "+str(now)+ " start " + str(start_time) )

    if now < (start_time + SNOOZE):
        #print( "\tSKIP " )
        return

    start_time = time.time();
    #print( "\tSend" )

    mqttudp.engine.send_publish(TRIGGER_TOPIC, "Warning, voltage is too high: " + value )

    #time.sleep(2)


if __name__ == "__main__":
    print( "Will send trigger if topic value is too high" )

    mqttudp.engine.listen(recv_packet)


