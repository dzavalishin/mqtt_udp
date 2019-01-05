#!/usr/bin/env python3

#               NB!! Broken!


import openhab


def extract_content(self, content):
    """ extract the "members" or "items" from content, and make a list """

    # sitemap items have "id" and "widget" keys. "widget is a list of "item" dicts. no "type" key.
    # items items have a "type" key which is something like "ColorItem", "DimmerItem" and so on, then "name" and "state". they are dicts
    # items groups have a "type" "GroupItem", then "name" and "state" (of the group) "members" is a list of item dicts as above
    
    if "type" in content:                   #items response
        if content["type"] == "GroupItem":
            # At top level (for GroupItem), there is type, name, state, link and members list
            members = content["members"]    #list of member items
        elif content["type"] == "item":
            members = content["item"]       #its a single item dict *not sure this is a thing* 
        else:
            members = content               #its a single item dict
    elif "widget" in content:               #sitemap response
        members = content["widget"]["item"] #widget is a list of items, (could be GroupItems) these are dicts
    elif "item" in content:
        members = content["item"]           #its a single item dict
    else:
        members = content                   #don't know...
    #log.debug(members)
    
    if isinstance(members, dict):   #if it's a dict not a list
        members = [members]         #make it a list (otherwise it's already a list of items...)
        
    return members



def listener(msg):
    print("msg="+str(msg))
    #members = extract_content(msg)
    #print(members)


if __name__ == "__main__":
    oh = openhab.OpenHab()
    oh.set_poll_listener(listener)

    #oh.get_status("gPersist")

    #oh.get_status_stream("/rest/sitemaps/default/0000") #connects, but empty

    # next ones work

    #oh.get_status_stream("/rest/sitemaps/default")
    oh.get_status_stream("/rest/items/CCU825_Sound_1")



