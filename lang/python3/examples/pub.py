#!/usr/bin/env python3

'''
	This program will publish one message. Topic is argv[1], value is argv[2]
'''

# will work even if package is not installed 
import sys
sys.path.append('..')
sys.path.append('../mqttudp')

import mqttudp.engine


if __name__ == "__main__":
    mqttudp.engine.send_publish_packet( sys.argv[1], sys.argv[2] )
