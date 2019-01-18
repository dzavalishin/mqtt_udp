#!/usr/bin/env python3

'''
@author: dz

Listen to all traffic on MQTT/UDP, pump updates to OpenHAB

Based on examples from https://github.com/openhab/openhab1-addons/wiki/Samples-REST

'''

# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

#import requests
import mqttudp.engine
import mqttudp.config as cfg

import mqttudp.openhab as openhab

cfg.set_group('openhab-gate')
log = cfg.log


oh = openhab.RestIO()
oh.set_host( cfg.get('host' ) )
oh.set_port( cfg.get('port' ) )





last = {}
def recv_packet_from_udp(ptype,topic,value,pflags,addr):
    if ptype != "publish":
        return
    if last.__contains__(topic) and last[topic] == value:
        return
    last[topic] = value
    #print( topic+"="+value )
    log.info( "To OpenHAB "+topic+"="+value )
    #put_status(topic, value)
    oh.post_command(topic, value)


if __name__ == "__main__":
    print( "Will resend all the MQTT/UDP traffic to OpenHAB host " + cfg.get('host' ) )
    mqttudp.engine.listen(recv_packet_from_udp)











