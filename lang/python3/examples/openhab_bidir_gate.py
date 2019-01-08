#!/usr/bin/env python3

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import threading
import time

import mqttudp.engine
import mqttudp.interlock

import mqttudp.openhab as openhab
import mqttudp.config as cfg


cfg.setGroup('openhab-gate')

blackList       = cfg.get('blacklist' )
sitemap         = cfg.get('sitemap' )

verbose         = cfg.getboolean('verbose' )


dc = openhab.Decoder()





# do not repeat item in 10 seconds if value is the same
it = mqttudp.interlock.Timer(10)

def listener(msg):

    dc.new_items = {}
    #print("msg="+str(msg))

    dc.extract_content(msg)
    #print(new_items)
    for topic in dc.new_items:
        value = dc.new_items[topic]
        #print( topic+"="+value )
        if cfg.check_black_list(topic, blackList):
            if verbose:
                print("From OpenHAB BLACKLIST "+ topic+" "+value)
            return

        if it.can_pass( topic, value ):
            if verbose:
                print("From broker "+topic+" "+value)
            mqttudp.engine.send_publish_packet( topic, value )
        else:
            if verbose:
                print("From broker REPEAT BLOCKED "+topic+" "+value)




def ohb_listen_thread():
    oh = openhab.RestIO()
    oh.set_poll_listener(listener)

    oh.set_host( cfg.get('host' ) )
    oh.set_port( cfg.get('port' ) )


    while True:
        oh.get_status("/rest/sitemaps/default")
        time.sleep( 1 ) # not more than once a second





if __name__ == "__main__":
    print('''Bidirectional OpenHAB + MQTT/UDP gate.
Will translate all topics btween OpenHAB and MQTT/UDP.''')


    olt = threading.Thread(target=ohb_listen_thread, args=(bclient,))
    ult = threading.Thread(target=udp_listen_thread, args=(bclient,))

    olt.start()
    ult.start()

    olt.join()
    ult.join()


