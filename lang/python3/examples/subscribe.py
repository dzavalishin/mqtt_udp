#!/usr/bin/env python3

'''
	This program will publish one SUBSCRIBE message. Topic is argv[1]
'''

# will work even if package is not installed 
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import mqttudp.engine


if __name__ == "__main__":
    mqttudp.engine.send_subscribe( sys.argv[1] )
