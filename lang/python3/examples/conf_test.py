#!/usr/bin/env python3

import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import mqttudp.config as cfg

cfg.dump()


print( cfg.config.getint('openhab-gate','port' ) )

