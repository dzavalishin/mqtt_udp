'''
Created on 24.12.2017

@author: dz
'''
import datetime

class bidirectional(object):
    '''
    Bidirectional interlock to protect from message loops
    when pumping data from UDP to broker and back.
    
    Basically locks in one direction for each topic for
    some time
    '''


    def __init__(self, timeout=5):
        '''
        timeout is in seconds, time for backwards message to be allowed after
        '''
        self.timeout = timeout
        self.dirmap = {}
        
        
    def bidir_lock(self, topic, value, direction):
        '''
        Returns true if message can pass
        '''
        now = datetime.datetime.now()
        
        if not self.dirmap.has_key(topic):
            self.dirmap[topic] = (value,now,direction)
            return True
        
        st_value,st_time,st_dir = self.dirmap[topic]
        self.dirmap[topic] = (value,now,direction) # update
        
        if st_dir == direction:
            return True
        
        # last transaction was in reverse direction, need to check timeout
        
        delta = now - st_time  

        if delta.total_seconds() > self.timeout:
            return True
        
        return False
        
        
    def broker_to_udp(self, topic, value):
        return self.bidir_lock( topic, value, "to_udp")
    
    def udp_to_broker(self, topic, value):
        return self.bidir_lock( topic, value, "from_udp")
        
        
        
        
        
        
        