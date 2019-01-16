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
snooze = 5


def recv_packet(ptype,topic,value,pflags,addr):
    if ptype != "publish":
        return

    if topic != LISTEN_TOPIC:
        return

    print( "Got " + value )

    iv = float( value )
    if iv < VALUE_LIMIT:
        return

    print( "Send "+value )

    mqttudp.engine.send_publish(TRIGGER_TOPIC, "Waning, voltage is too high: " + value )

    #time.sleep(2)


if __name__ == "__main__":
    print( "Will send trigger if topic value is too high" )

    mqttudp.engine.listen(recv_packet)


