#!/usr/bin/env python3

# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import threading
import time

import mqttudp.engine
import mqttudp.interlock

import mqttudp.openhab as openhab
import mqttudp.config as cfg


cfg.set_group('openhab-gate')
log = cfg.log

blackList       = cfg.get('blacklist' )
sitemap         = cfg.get('sitemap' )

verbose         = cfg.getboolean('verbose' )


oh = openhab.RestIO()

dc = openhab.Decoder()


# back/forth loop lock
ilock = mqttudp.interlock.Bidirectional(5)

# do not repeat item in 10 seconds if value is the same
it_to_udp = mqttudp.interlock.Timer(10)
it_to_ohb = mqttudp.interlock.Timer(10)




# ------------------------------------------------------------------------
#
# From OpenHAB to UDP
#
# ------------------------------------------------------------------------


# TODO logging
def send_to_udp( topic, value ):
    if not it_to_udp.can_pass( topic, value ):
#        if verbose:
#            print("From OpenHAB REPEAT BLOCKED "+topic+" "+value)
        log.info("From OpenHAB REPEAT BLOCKED "+topic+" "+value)
        return

    if not ilock.broker_to_udp( topic, value ):
#        if verbose:
#            print("From OpenHAB BLOCKED: "+topic+"="+value)
        log.info("From OpenHAB BLOCKED: "+topic+"="+value)
        return

    if cfg.check_black_list(topic, blackList):
#        if verbose:
#            print("From OpenHAB BLACKLIST "+topic+" "+value)
        log.info("From OpenHAB BLACKLIST "+topic+" "+value)
        return

#    if verbose:
#        print("From OpenHAB "+topic+" "+value)
    log.info("From OpenHAB "+topic+" "+value)

    mqttudp.engine.send_publish( topic, value )




def listener(msg):

    dc.new_items = {}

    dc.extract_content(msg)

    for topic in dc.new_items:
        value = dc.new_items[topic]
        send_to_udp( topic, value )



def ohb_listen_thread():
    oh.set_poll_listener(listener)

    oh.set_host( cfg.get('host' ) )
    oh.set_port( cfg.get('port' ) )


    while True:
        oh.get_status("/rest/sitemaps/"+sitemap)
        #oh.get_status("/rest/sitemaps/default")
        time.sleep( 1 ) # not more than once a second


# ------------------------------------------------------------------------
#
# From UDP to OpenHAB
#
# ------------------------------------------------------------------------

#todo use mqttudp.interlock.Timer too
#last = {}
def recv_packet_from_udp(ptype,topic,value,pflags,addr):

    if ptype != "publish":
        return

    if not it_to_ohb.can_pass( topic, value ):
#        if verbose:
#            print("To OpenHAB REPEAT BLOCKED "+topic+" "+value)
        log.info("To OpenHAB REPEAT BLOCKED "+topic+" "+value)

    #if last.__contains__(topic) and last[topic] == value:
    #    return

    #last[topic] = value

    if cfg.check_black_list(topic, blackList):
#        if verbose:
#            print("To OpenHAB BLACKLIST "+ topic+" "+value)
        log.info("To OpenHAB BLACKLIST "+ topic+" "+value)
        return

    if not ilock.udp_to_broker(topic, value):
#        print( "To OpenHAB BLOCKED: "+topic+"="+value )
        log.info( "To OpenHAB BLOCKED: "+topic+"="+value )
        return

#    print( "To OpenHAB "+topic+"="+value )
    log.info( "To OpenHAB "+topic+"="+value )
    #oh.put_status(topic, value)
    oh.post_command(topic, value)



def udp_listen_thread():
    mqttudp.engine.listen(recv_packet_from_udp)




# ------------------------------------------------------------------------
#
# Main
#
# ------------------------------------------------------------------------


if __name__ == "__main__":
    print('''Bidirectional OpenHAB + MQTT/UDP gate.
Will translate all topics btween OpenHAB and MQTT/UDP.''')
    #print("OpenHAB SiteMap='"+sitemap+"'")
    log.info("OpenHAB SiteMap='"+sitemap+"'")


    olt = threading.Thread(target=ohb_listen_thread, args=())
    ult = threading.Thread(target=udp_listen_thread, args=())

    olt.start()
    ult.start()

    olt.join()
    ult.join()


