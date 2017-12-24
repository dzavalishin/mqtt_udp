'''
Created on 24.12.2017

@author: dz

Listen to all traffic on MQTT/UDP, print
'''
# will work even if package is not installed 
import sys
sys.path.append('..')

import mqttudp.sub

if __name__ == "__main__":
    s = mqttudp.sub.make_recv_socket()
    last = {}
    while True:
        pkt = mqttudp.sub.recv_udp_packet(s)    
        topic,value = mqttudp.sub.parse_packet(pkt)
        if last.has_key(topic) and last[topic] == value:
            continue
        last[topic] = value
        print topic+"="+value
