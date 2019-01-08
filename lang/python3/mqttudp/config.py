import configparser
import re

config = configparser.ConfigParser()


config['DEFAULT'] = {
    'verbose' : True    # debug mode, application will chat a lot
    }


config['openhab-gate'] = {
    'port': "8080",
    'blacklist' : '^\\$'   # regexp: prevent matching topics to come through, match on MQTT/UDP side
    }

config['mqtt-gate'] = {
#    'host': 'smart.',
    'port': 1883,
    'subscribe': '#',
#    'convertfrom': '',  # regexp to convert topic from mqtt to UDP
#    'convertfrom': '',  # regexp to convert topic from mqtt to UDP
#    'convertto': '',    # regexp to convert topic from UDP to mqtt
#    'convertto': '',    # regexp to convert topic from UDP to mqtt
    'blacklist' : '^\\$'   # regexp: prevent matching topics to come through, match on MQTT/UDP side
    }


config.read('mqtt-udp.ini')


#for key in config['mqtt-gate']:  
#    print(key)
def dump():
    for sec in config:  
        print( '['+ sec + ']' )
        for key in config[sec]:
            print( '\t' + key + "=" + config[sec][key] )


caller_group = ''

def setGroup(group):
    global caller_group
    caller_group = group

def set_group(group):
    global caller_group
    caller_group = group

def get(item):
    global caller_group
    return config.get( caller_group, item )

def getboolean(item):
    #global caller_group
    return config.getboolean( caller_group, item )



def check_black_list(topic,blacklist):
    #print(topic,blacklist)
    return (len(blacklist) > 0) and (re.match( blacklist, topic ))



