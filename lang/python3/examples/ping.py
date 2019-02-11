#!/usr/bin/env python3

'''
	This program will send PING packet and wait for replies
'''

# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import threading
import mqttudp.engine
import time



def ping_thread():
    while True:
        mqttudp.engine.send_ping()
        time.sleep(1)



def recv_packet(pkt):
    if pkt.ptype != mqttudp.engine.PacketType.Publish:
        print( str(pkt.ptype) + ", " + pkt.topic + "\t\t" + str(pkt.addr) )
        return
    print( pkt.topic+"="+pkt.value+ "\t\t" + str(pkt.addr) )



if __name__ == "__main__":
    print( "Will send MQTT/UDP ping packet and dump all the replies forever" )
    print( "Press ^C to stop" )

    mqttudp.engine.set_muted( True )

    mqttudp.engine.send_ping()

    pt = threading.Thread(target=ping_thread, args=())
    pt.start()

    mqttudp.engine.listen(recv_packet)







