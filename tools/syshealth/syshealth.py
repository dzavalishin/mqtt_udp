#!/usr/bin/env python3

# will work even if package is not installed
import sys
sys.path.append('../../lang/python3')
#sys.path.append('../mqttudp')

#import threading
#import time

#import mqttudp.engine
#import mqttudp.interlock

#import mqttudp.openhab as openhab
import mqttudp.config as cfg


cfg.set_group('syshealth')
log = cfg.log

#blackList       = cfg.get('blacklist' )
#sitemap         = cfg.get('sitemap' )

verbose         = cfg.getboolean('verbose' )


if __name__ == "__main__":
    print('''Send this machine/OS status to MQTT/UDP gate.''')
    #log.info("OpenHAB SiteMap='"+sitemap+"'")
    log.info("SysHealth start")

