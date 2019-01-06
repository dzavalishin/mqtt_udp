#!/usr/bin/env python3

#               NB!! Broken!


import openhab


def process_item( topic, value ):
    print(topic+"="+value)


def extract_item(i):
    #print("item="+str(hp))
    process_item( i["name"], i["state"] )

def extract_widget(data):
    print("wi="+str(data))
    if not data.__contains__("widget"):
        print("unknown="+str(data))
        return

    wi = data["widget"]

    for wiel in wi:
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
            process_item( content["name"], content["state"] )
        elif ct == "SwitchItem":
            #members = content["item"]       #its a single item dict *not sure this is a thing*
            process_item( content["name"], content["state"] )
        else:
            members = content               #its a single item dict
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
    
    if isinstance(members, dict):   #if it's a dict not a list
        members = [members]         #make it a list (otherwise it's already a list of items...)
        
    return members



def listener(msg):
    #print("msg="+str(msg))
    print("")
    extract_content(msg)
    #print(members)


if __name__ == "__main__":
    oh = openhab.OpenHab()
    oh.set_poll_listener(listener)

    #oh.get_status("gPersist")

    #oh.get_status_stream("/rest/sitemaps/default/0000") #connects, but empty

    # next ones work

    oh.get_status_stream("/rest/sitemaps/default")
    #oh.get_status_stream("/rest/items/CCU825_Sound_1")
    #oh.get_status_stream("/rest/items/gPersist")



