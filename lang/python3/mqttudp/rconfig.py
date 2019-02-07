#
# Passive remote configuration support
#
# See https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-message-content-specification
#

# will work even if package is not installed
import sys
sys.path.append('..')

import mqttudp.engine as mq
import mqttudp.mqtt_udp_defs as defs

__INIT_ITEMS = None
__conf_items = {}

__MY_ID = "020002000200" # TODO generate me

def init( init_items ):
    global __INIT_ITEMS
    global __conf_items
    __INIT_ITEMS = init_items

        # Load all, then insert absent and r/o ones from init
    load_all()

    for k in init_items:
        v = init_items[k]
        print( "Init " + k + " = " + v )
        if not __conf_items.__contains__(k):
            __conf_items[k] = v
            print( "Set " + k + " = " + v )
        else:
            if k[0, 3] == "info":
                __conf_items[k] = v
                print( "Set info" + k + " = " + v )


def recv_one_item( k, v, topic, value ):
        if full_topic(k) == topic:
                #v[1] = value
            print( "Got "+k+" = '"+value+"'" )
            __conf_items[k] = value
            # TODO call user hook
            save_all() # TODO TEMP, kill me


def on_publish ( topic, value ):
    for k in __conf_items:
        v = __conf_items[k]
        recv_one_item( k, v, topic, value )




def recv_packet(ptype,topic,value,pflags,addr):
    if ptype == "publish":
        print( "pub "+topic+"="+value+ "\t\t" + str(addr) )
        on_publish( topic, value )
        return

    if ptype == "subscribe":
        print( "sub "+ptype + ", " + topic + "\t\t" + str(addr) )
        send_asked_rconf_items( topic )
        return



def send_asked_rconf_items( topic ):
    """
    Send out remote config items according to 
    subscribe reques (possibly wildcard).
    """
    for key in __conf_items:
        if mq.match( topic, full_topic(key) ):
            print( "Send "+key+"="+ __conf_items[key] )
            send_one_item( key, __conf_items[key] )
        
def send_one_item( k, v ):
    """ 
    Send one remote config item value
    """
    mq.send_publish( full_topic(k), v )


def full_topic( topic : str ):
    """
    Return full remote config topic name for given suffix
    (kind/name).
    """
    return defs.SYS_CONF_PREFIX+"/"+__MY_ID+"/"+topic

def load_all():
    pass

def same_all():
    pass





