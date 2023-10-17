#import threading
import mqttudp.engine
#import random
import urandom
import time

TOPIC="random_data"


print( "Will send MQTT/UDP packets with random number as a payload" )
print( "Topic is '"+TOPIC+"'" )

while True:
    n = str(urandom.getrandbits(5))
    print( "Send "+str( n) )
    mqttudp.engine.send_publish( TOPIC, n )
    time.sleep(2)
