#!/usr/bin/env python3

import requests
import json
import base64
import time

#import logging as log
import logging
 
# add filemode="w" to overwrite
#log.basicConfig(filename="openhab_gate.log", level=log.INFO)
log = logging.getLogger("mqtt-udp.openhab")



class RestIO:

    def __init__(self):
        """Constructor"""

        self.openhab_host = "smart."
        self.openhab_port = "8080"
        self.connected = True

        self.username = ""
        self.password = ""



    def put_status( self, key, value ):
        """ Put a status update to OpenHAB  key is item, value is state """
        url = 'http://%s:%s/rest/items/%s/state'%(self.openhab_host, self.openhab_port, key)
        req = requests.put(url, data=value, headers=self.basic_header())
        if req.status_code != requests.codes.ok:
            print( "Can't reach "+url )
    #        req.raise_for_status()     


    def post_command( self, key, value ):
        """ Post a command to OpenHAB - key is item, value is command """
        url = 'http://%s:%s/rest/items/%s'%(self.openhab_host, self.openhab_port, key)
        req = requests.post(url, data=value, headers=self.basic_header())
        if (req.status_code != requests.codes.ok) and (req.status_code != 201):
            print( "Can't reach "+url+" code="+str(req.status_code)+", text '"+req.text+"'" )
            #req.raise_for_status()




    def set_poll_listener(self,listener):
        self.poll_listener = listener

    def get_status(self, name):
        """
         Request updates for any item in group NAME from OpenHAB.
         Long-polling will not respond until item updates.
        """
        # When an item in Group NAME changes we will get all items in the group
        # and need to determine which has changed
        url = 'http://%s:%s%s'%(self.openhab_host, self.openhab_port, name)
        payload = {'type': 'json'}
        try:
            #print("request "+url)
            req = requests.get(url, params=payload,
                                headers=self.polling_header())
            if req.status_code != requests.codes.ok:
                req.raise_for_status()
            # Try to parse JSON response
            ##print(req.json())
            self.poll_listener(req.json())
        except Exception as e:
            print(e)

    def make_auth(self):
        userpass = '%s:%s'%(self.username, self.password)
        userpass = userpass.encode()
        self.auth = base64.encodestring(userpass).decode('utf-8').replace('\n', '')
        #print(self.auth)


    def streaming_header(self):
        self.make_auth()
        # Header for OpenHAB REST request - streaming
        return {
#            "Authorization" : "Basic %s" % self.cmd.auth,
            "X-Atmosphere-Transport" : "streaming",
#            "X-Atmosphere-tracking-id" : self.atmos_id,
            "X-Atmosphere-Framework" : "1.0",
            "Accept" : "application/json"}

    def polling_header(self):
        self.make_auth()
        # Header for OpenHAB REST request - polling
        return {
#            "Authorization" : "Basic %s" % self.cmd.auth,
            "X-Atmosphere-Transport" : "long-polling",
#            "X-Atmosphere-tracking-id" : self.atmos_id,
            "X-Atmosphere-Framework" : "1.0",
            "Accept" : "application/json"}
    
    def basic_header(self):
        self.make_auth()
        # Header for OpenHAB REST request - standard
        return {
#                "Authorization" : "Basic %s" %self.auth,
                "Content-type": "text/plain"}
    


    def get_status_stream(self, item):
        '''
        NB. Broken.

        Request updates for any item in item from OpenHAB.
        streaming will not respond until item updates. Can also use 
        Sitemap page id (eg /rest/sitemaps/name/0000) as long as it
        contains items (not just groups of groups)
        auto reconnects while parent.connected is true.
        This is meant to be run as a thread.
        '''
        
        connect = 0     #just keep track of number of disconnects/reconnects
        
        url = 'http://%s:%s%s'%(self.openhab_host,self.openhab_port, item)
        payload = {'type': 'json'}
        while self.connected:
            if connect == 0:
                log.info("Starting streaming connection for %s" % url)
            else:
                log.info("Restarting (#%d) streaming connection after disconnect for %s" % (connect, url))
                time.sleep(2) # TODO hack we skip a second because connection restarts just after reading everything sent. that is wrong.
            try:
                req = requests.get(url, params=payload, timeout=(310.0, 310),   #timeout is (connect timeout, read timeout) note! read timeout is 310 as openhab timeout is 300
                                headers=self.streaming_header(), stream=True)
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
                    message = b''
                    #message = ''
                    content = {}
                    #print(req.content)

                    if True:

                        for char in req.iter_content(decode_unicode=True):   #read content 1 character at a time
                            try:
                                if char: # todo check overflow message len
                                    #log.debug(char)
                                    #message += str(char)
                                    message += char
                                    if str(char) != '}': # todo check overflow message len
                                        continue
                                    content = json.loads(message.decode('utf-8'))
                                    break

                            except ValueError:      #keep reading until json.loads returns a value
                                pass
                    else:

                        for line in req.iter_lines(decode_unicode=True):
                            if line:
                                js = json.loads(line)
                                #print("line="+js)
                                self.poll_listener(js)
                                #log.info(js)

                    #message = str(message)
                    message = message.decode('utf-8')

                    #log.error("msg="+message)
                    content = json.loads(message)
                        
                    if len(content) == 0:
                        raise requests.exceptions.ConnectTimeout("Streaming connection dropped")

                    #log.debug(content)
                    #print(content)
                    self.poll_listener(content)

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


    # --------------------------------------------------------------------
    #
    # Getters/Setters
    #
    # --------------------------------------------------------------------



    def set_host(self, host):
        self.openhab_host = host

    def set_port(self, port):
        self.openhab_port = port




class Decoder:
    ''' Decode OpenHAB JSON '''

    def __init__(self):
        """Constructor"""
        self.new_items = {}
    
    def process_item(self,  topic, value, ptype ):
        if ptype == "NumberItem":
            try:
                num = float(value)
                value = str(num)
            except Exception:
                pass
        if not self.new_items.__contains__(topic):
            #print(topic+"="+value)
            self.new_items[topic]=value
    
    
    def extract_item(self, i):
        #print("item="+str(hp))
        self.process_item( i["name"], i["state"], i["type"] )
    
    # one widget or array
    def extract_widget(self, data):
        #print("wia="+str(data))
        wi = data["widget"]
    
        #if not data.__contains__("widgetId"):
        #    #print("unknown="+str(data))
        #    self.extract_widget_el(data)
        #    return
    
        if isinstance(wi, dict):
            #print("unknown="+str(data))
            self.extract_widget_el(wi)
            return
    
    
        for wiel in wi:
            self.extract_widget_el(wiel)
    
    # one widget exactly
    def extract_widget_el(self, wiel):
        #print("wiel="+str(wiel))
    
        #print("wiel="+str(wiel))
        #if "widget" in wiel:
        if wiel.__contains__("widget"):
            self.extract_widget(wiel)
        #if "linkedPage" in wiel:
        elif wiel.__contains__("linkedPage"):
            self.extract_widget(wiel["linkedPage"])
        #elif "item" in wiel:
        elif wiel.__contains__("item"):
            self.extract_item(wiel["item"])
        else:
            print("unknown []="+str(data))
    
    def extract_content(self, content):
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
                self.process_item( content["name"], content["state"], ct )
            elif ct == "SwitchItem":
                self.process_item( content["name"], content["state"], ct )
            else:
                #members = content               #its a single item dict
                pass
        elif "homepage" in content:               #sitemap response
            hp=content["homepage"]
            #print(wi)
            self.extract_widget(hp)
        elif "widget" in content:               #sitemap response
            #print(wi)
            self.extract_widget(content)
        elif "item" in content:
            self.extract_item(content["item"])
        else:
            #log.debug(members)
            print("unknown format: "+str(content))
    
    
    
