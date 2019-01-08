#!/usr/bin/env python3

# will work even if package is not installed
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import threading
import time
#import re

import mqttudp.engine
import mqttudp.interlock

import openhab
import mqttudp.config as cfg


cfg.setGroup('openhab-gate')

blackList=cfg.get('blacklist' )

verbose = cfg.getboolean('verbose' )



# actually, interlock.Timer does it
new_items = {}

def process_item( topic, value, ptype ):
    global new_items
    if ptype == "NumberItem":
        try:
            num = float(value)
            value = str(num)
        except Exception:
            pass
    if not new_items.__contains__(topic):
        #print(topic+"="+value)
        new_items[topic]=value


def extract_item(i):
    #print("item="+str(hp))
    process_item( i["name"], i["state"], i["type"] )

# one widget or array
def extract_widget(data):
    #print("wia="+str(data))
    wi = data["widget"]

    #if not data.__contains__("widgetId"):
    #    #print("unknown="+str(data))
    #    extract_widget_el(data)
    #    return

    if isinstance(wi, dict):
        #print("unknown="+str(data))
        extract_widget_el(wi)
        return


    for wiel in wi:
        extract_widget_el(wiel)

# one widget exactly
def extract_widget_el(wiel):
    #print("wiel="+str(wiel))

    #print("wiel="+str(wiel))
    #if "widget" in wiel:
    if wiel.__contains__("widget"):
        extract_widget(wiel)
    #if "linkedPage" in wiel:
    elif wiel.__contains__("linkedPage"):
        extract_widget(wiel["linkedPage"])
    #elif "item" in wiel:
    elif wiel.__contains__("item"):
        extract_item(wiel["item"])
    else:
        print("unknown []="+str(data))

def extract_content(content):
    """ extract the "members" or "items" from content, and make a list """

    # sitemap items have "id" and "widget" keys. "widget is a list of "item" dicts. no "type" key.
    # items items have a "type" key which is something like "ColorItem", "DimmerItem" and so on, then "name" and "state". they are dicts
    # items groups have a "type" "GroupItem", then "name" and "state" (of the group) "members" is a list of item dicts as above

    
    if "type" in content:                   #items response

        ct = content["type"]
        #print("type="+ct)

        if ct == "GroupItem":
            # At top level (for GroupItem), there is type, name, state, link and members list
            #members = content["members"]    #list of member items
            pass
        elif ct == "NumberItem":
            #members = content["item"]       #its a single item dict *not sure this is a thing*
            process_item( content["name"], content["state"], ct )
        elif ct == "SwitchItem":
            #members = content["item"]       #its a single item dict *not sure this is a thing*
            process_item( content["name"], content["state"], ct )
        else:
            #members = content               #its a single item dict
            pass
    elif "homepage" in content:               #sitemap response
        hp=content["homepage"]
        #print(wi)
        extract_widget(hp)
    elif "widget" in content:               #sitemap response
        #print(wi)
        #members = content["widget"]["item"] #widget is a list of items, (could be GroupItems) these are dicts
        extract_widget(content)
    elif "item" in content:
        #members = content["item"]           #its a single item dict
        extract_item(content["item"])
    else:
        #members = content                   #don't know...
        #log.debug(members)
        print("unknown format: "+str(content))
    
    #if isinstance(members, dict):   #if it's a dict not a list
    #    members = [members]         #make it a list (otherwise it's already a list of items...)
        
    #return members


# do not repeat item in 10 seconds if value is the same
it = mqttudp.interlock.Timer(10)

def listener(msg):
    global new_items
    new_items = {}
    #print("msg="+str(msg))
    #print("")
    extract_content(msg)
    #print(new_items)
    for topic in new_items:
        value = new_items[topic]
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


if __name__ == "__main__":
    oh = openhab.OpenHab()
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



