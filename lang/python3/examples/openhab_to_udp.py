#!/usr/bin/env python3

# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

#import threading
import time
#import re

import mqttudp.engine
import mqttudp.interlock

import mqttudp.openhab as openhab
import mqttudp.config as cfg


cfg.set_group('openhab-gate')
log = cfg.log

blackList=cfg.get('blacklist' )

verbose = cfg.getboolean('verbose' )

dc = openhab.Decoder()





# do not repeat item in 10 seconds if value is the same
it = mqttudp.interlock.Timer(10)

def listener(msg):
    #global new_items
    dc.new_items = {}
    #print("msg="+str(msg))
    #print("")
    dc.extract_content(msg)
    #print(new_items)
    for topic in dc.new_items:
        value = dc.new_items[topic]
        #print( topic+"="+value )
        if cfg.check_black_list(topic, blackList):
            log.info("From OpenHAB BLACKLIST "+ topic+" "+value)
            #if verbose:
            #    print("From OpenHAB BLACKLIST "+ topic+" "+value)
            return

        if it.can_pass( topic, value ):
            log.info("From broker "+topic+" "+value)
            #if verbose:
            #    print("From broker "+topic+" "+value)
            mqttudp.engine.send_publish( topic, value )
        else:
            log.info("From broker REPEAT BLOCKED "+topic+" "+value)
            #if verbose:
            #    print("From broker REPEAT BLOCKED "+topic+" "+value)


if __name__ == "__main__":
    oh = openhab.RestIO()
    oh.set_poll_listener(listener)

    oh.set_host( cfg.get('host' ) )
    oh.set_port( cfg.get('port' ) )

    print('''Gate from OpenHAB tp MQTT/UDP.
Will translate all updated topics to MQTT/UDP.''')

    if True:
        while True:
            #oh.get_status("/rest/items/CCU825_Sound_1")
            oh.get_status("/rest/sitemaps/default")
            time.sleep( 1 ) # not more than once a second

    else:
    
        #oh.get_status_stream("/rest/sitemaps/default/0000") #connects, but empty
    
        # next ones work
    
        oh.get_status_stream("/rest/sitemaps/default")
        #oh.get_status_stream("/rest/items/CCU825_Sound_1")
    
        # broken
        #oh.get_status_stream("/rest/items/gPersist")



