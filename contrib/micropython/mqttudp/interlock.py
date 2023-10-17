'''
Created on 24.12.2017

@author: dz
'''
import datetime

class Bidirectional(object):
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
        
        if not self.dirmap.__contains__(topic):
            self.dirmap[topic] = (value,now,direction)
            return True
        
        #st_value,st_time,st_dir = self.dirmap[topic]
        _,st_time,st_dir = self.dirmap[topic]
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
        
        
        

class Timer(object):
    '''
    Timer lock used to prevent too frequent update.
    
    It will check for previous value and update time,
    and let update if value is different or time spent.
    '''


    def __init__(self, timeout=20):
        '''
        timeout is in seconds, time for duplicate value can
        be published again
        '''
        self.timeout = timeout
        self.dirmap = {}
        
        
    def can_pass(self, topic, value ):
        '''
        Returns true if message can pass
        '''
        now = datetime.datetime.now()

        # not yet - pass
        if not self.dirmap.__contains__(topic):
            self.dirmap[topic] = (value,now)
            return True
        
        st_value,st_time = self.dirmap[topic]

        # value is different - TODO for numerics add delta to check within
        if st_value != value:
            self.dirmap[topic] = (value,now) # update
            return True
        
        # value is the same, check time spent
        
        delta = now - st_time  

        if delta.total_seconds() > self.timeout:
            self.dirmap[topic] = (value,now) # update
            return True
        
        return False
        
        
    # TODO def get_timed_out( self ) # Return list of timed out items to resend them if no update comes through too long
        
        


        
        
        
        