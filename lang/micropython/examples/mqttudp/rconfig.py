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

import configparser
import uuid

__store = configparser.ConfigParser()

#__INIT_ITEMS = None
__conf_items = {}
__on_config = None

#__MY_ID = "020002000200" # TODO generate me
__MY_ID = str( uuid.uuid4() )

def init( init_items ):
    #global __INIT_ITEMS
    global __conf_items
    #__INIT_ITEMS = init_items

        # Load all, then insert absent and r/o ones from init
    load_all()

    for k in init_items:
        v = init_items[k]
        #print( "Init " + k + " = " + v )
        if not __conf_items.__contains__(k):
            __conf_items[k] = v
            #print( "Set " + k + " = " + v )
        else:
            if k[0:4] == "info/":
                __conf_items[k] = v
                #print( "Set info" + k + " = " + v )


def recv_one_item( k, v, topic, value ):
        if full_topic(k) == topic:
                #v[1] = value
            #print( "Got "+k+" = '"+value+"'" )
            __conf_items[k] = value
            save_all() # TODO TEMP, kill me
            # call user hook
            if not (__on_config == None):
                __on_config(topic,value)


def on_publish ( topic, value ):
    for k in __conf_items:
        v = __conf_items[k]
        recv_one_item( k, v, topic, value )




def recv_packet(pkt):
    if pkt.ptype == mq.PacketType.Publish:
    #if ptype == "publish":
        #print( "pub "+topic+"="+value+ "\t\t" + str(addr) )
        on_publish( pkt.topic, pkt.value )
        return

    #if ptype == "subscribe":
    if pkt.ptype == mq.PacketType.Subscribe:
        #print( "sub "+ptype + ", " + topic + "\t\t" + str(addr) )
        send_asked_rconf_items( pkt.topic )
        return



def send_asked_rconf_items( topic ):
    """
    Send out remote config items according to 
    subscribe reques (possibly wildcard).
    """
    for key in __conf_items:
        if mq.match( topic, full_topic(key) ):
            #print( "Send "+key+"="+ __conf_items[key] )
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



__cfg_file_name = "remote_config.ini"
__INI_SECTION = "remote"

def set_ini_file_name(fn):
    """
    Set name of file to store remote config state in
    """
    global __cfg_file_name
    __cfg_file_name = fn

def load_all():
    __store.read(__cfg_file_name)
    if not __store.__contains__(__INI_SECTION):
        return
    remote = __store[__INI_SECTION]
    for key in remote:
        __conf_items[key] = remote[key]


# TODO we receive echo of our pubs and re-save on each!
def save_all():
    __store[__INI_SECTION] = {}
    remote = __store[__INI_SECTION]
    for key in __conf_items:
        val = __conf_items[key]
        remote[key] = val

    with open(__cfg_file_name, 'w') as configfile:
        __store.write(configfile)


def publish_for( topic_of_topic, data ):
    """
     Send message using configurable topic

     Get value of "$SYS/conf/{MY_ID}/topic_of_topic" and use it as topic to send data

     @param #string topic_of_topic name of parameter holding topic used to send message
     @param #string data data to send

    """
    key = "topic/"+topic_of_topic
    if not __conf_items.__contains__(key):
        #print( "no configured value (topic) for topic_of topic() "+key+"'" )
        return

    item = __conf_items[key]
    mq.send_publish( item, data )

def is_for( topic_of_topic, topic ):
    """
    true if value for topic_of_topics == topic
    test incoming message topic to be for this configurable
    """
    key = "topic/"+topic_of_topic

    if not __conf_items.__contains__(key):
        #print( "no configured value (topic) for topic_of topic() "+key+"'" )
        return False

    item = __conf_items[key]
    return topic == item

def get_setting( name ):
    if not __conf_items.__contains__(key):
        return None
    return __conf_items[key]   

def set_on_config( callback ):
    global __on_config
    __on_config = callback



