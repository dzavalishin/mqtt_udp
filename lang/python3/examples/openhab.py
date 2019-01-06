#!/usr/bin/env python3

#               NB!! Broken!

import requests
import json
import base64

import logging as log
 
# add filemode="w" to overwrite
log.basicConfig(filename="openhab_gate.log", level=log.INFO)
 

class OpenHab:

    def __init__(self):
        """Constructor"""
        print("c'tor")
        self.openhab_host = "smart."
        self.openhab_port = "8080"
        self.connected = True

    def set_poll_listener(self,listener):
        self.poll_listener = listener

    def get_status(self, name):
        """
         Request updates for any item in group NAME from OpenHAB.
         Long-polling will not respond until item updates.
        """
        # When an item in Group NAME changes we will get all items in the group
        # and need to determine which has changed
        url = 'http://%s:%s/rest/items/%s'%(self.openhab_host,
                                        self.openhab_port, name)
        payload = {'type': 'json'}
        try:
            req = requests.get(url, params=payload,
                                headers=self.polling_header())
            if req.status_code != requests.codes.ok:
                req.raise_for_status()
            # Try to parse JSON response
            # At top level, there is type, name, state, link and members array
            members = req.json()["members"]
            dump_members(members)
        except Exception as e:
            print(e)



    def dump_members(members):
        for member in members:
            # Each member has a type, name, state and link
            name = member["name"]
            state = member["state"]
            do_publish = True
            # Pub unless we had key before and it hasn't changed
            if name in self.prev_state_dict:
                if self.prev_state_dict[name] == state:
                    do_publish = False
            self.prev_state_dict[name] = state
            if do_publish:
                self.publish(name, state)


    def polling_header(self):
        """ Header for OpenHAB REST request - polling """
#        self.auth = base64.encodestring('%s:%s'%(self.username, self.password)).replace('\n', '')
        return {
#            "Authorization" : "Basic %s" % self.cmd.auth,
            "X-Atmosphere-Transport" : "long-polling",
#            "X-Atmosphere-tracking-id" : self.atmos_id,
            "X-Atmosphere-Framework" : "1.0",
            "Accept" : "application/json"}
    
    def basic_header(self):
        """ Header for OpenHAB REST request - standard """
#        self.auth = base64.encodestring('%s:%s'%(self.username, self.password)).replace('\n', '')
        return {
#                "Authorization" : "Basic %s" %self.auth,
                "Content-type": "text/plain"}
    


    def get_status_stream(self, item):
        """
        Request updates for any item in item from OpenHAB.
        streaming will not respond until item updates. Can also use 
        Sitemap page id (eg /rest/sitemaps/name/0000) as long as it
        contains items (not just groups of groups)
        auto reconnects while parent.connected is true.
        This is meant to be run as a thread
        """
        
        connect = 0     #just keep track of number of disconnects/reconnects
        
        url = 'http://%s:%s%s'%(self.openhab_host,self.openhab_port, item)
        payload = {'type': 'json'}
        while self.connected:
            if connect == 0:
                log.info("Starting streaming connection for %s" % url)
            else:
                log.info("Restarting (#%d) streaming connection after disconnect for %s" % (connect, url))
            try:
                req = requests.get(url, params=payload, timeout=(310.0, 310),   #timeout is (connect timeout, read timeout) note! read timeout is 310 as openhab timeout is 300
                                headers=self.polling_header(), stream=True)
                if req.status_code != requests.codes.ok:
                    log.error("bad status code")
                    req.raise_for_status()
                    
            except requests.exceptions.ReadTimeout as e: #see except UnboundLocalError: below for explanation of this
                if not self.connected:   # if we are not connected - time out and close thread, else retry connection.
                    log.error("Read timeout, exit: %s" % e)
                    break
                    
            except (requests.exceptions.HTTPError, requests.exceptions.ConnectTimeout, requests.exceptions.ConnectionError) as e:
                log.error("Error, exit: %s" % str(e))
                break
            #log.debug("received response headers %s" % req.headers)
            log.info("Data Received, streaming connection for %s" % url)
            connect += 1
            try:
                while self.connected:
                    message = ''
                    content = {}
                    #print(req.content)
                    '''if False:

                        for char in req.iter_content():   #read content 1 character at a time
                            try:
                                if char:
                                    #log.debug(char)
                                    message += str(char)
                                    content = json.loads(message)
                                    break
                                
                            except ValueError:      #keep reading until json.loads returns a value
                                pass
                    else:'''


                    for line in req.iter_lines(decode_unicode=True):
                        if line:
                            js = json.loads(line)
                            #print("line="+js)
                            self.poll_listener(js)
                            log.info(js)

                    #content = json.loads(req.content)

                    #if len(content) == 0:
                    #    raise requests.exceptions.ConnectTimeout("Streaming connection dropped")
                        
                    #log.debug(content)
                    #print(content)
                    #self.poll_listener(content)

                    #members = self.extract_content(content)
                    #print(members)
                    #self.publish_list(members)
            
            except UnboundLocalError:   #needed because streaming on single item does not time out normally - so thread hangs.
                pass
            #except (timeout, requests.exceptions.ConnectTimeout, requests.exceptions.ConnectionError) as e:
            except ( requests.exceptions.ConnectTimeout, requests.exceptions.ConnectionError) as e:
                #log.info("Socket/Read timeout: %s" % e.message)
                log.info("Socket/Read timeout: %s" % str(e))
            except Exception as e:
                #log.error("Stream Unknown Error: %s, %s" % (e, e.message))
                log.error("Stream Unknown Error: %s" % str(e))
                log.error("logging handled exception - see below")
                log.exception(e)
                
        log.info("Disconnected, exiting streaming connection for %s" % url)
        if item in self.streaming_threads:
            del(self.streaming_threads[item])
            log.debug("removed %s from streaming_threads" % item)

    '''
    def polling_header(self):
        """ Header for OpenHAB REST request - streaming """
        
        self.auth = base64.encodestring('%s:%s'
                        %(self.username, self.password)
                        ).replace('\n', '')
        return {
            #"Authorization" : "Basic %s" % self.auth,
            "X-Atmosphere-Transport" : "streaming",
            #"X-Atmosphere-tracking-id" : self.atmos_id,
            "Accept" : "application/json"}

    def basic_header(self):
        """ Header for OpenHAB REST request - standard """
        
        self.auth = base64.encodestring('%s:%s'
                        %(self.username, self.password)
                        ).replace('\n', '')
        return {
                #"Authorization" : "Basic %s" %self.auth,
                "Content-type": "text/plain"}
    '''

    def extract_content(self, content):
        """ extract the "members" or "items" from content, and make a list """

        print(content)

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

