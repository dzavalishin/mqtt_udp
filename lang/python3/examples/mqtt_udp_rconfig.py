#
# Passive remote configuration use example
#

# will work even if package is not installed
import sys
sys.path.append('..')
#sys.path.append('../mqttudp')

import mqttudp.engine as mq
import mqttudp.rconfig as rcfg

init_items = {
	## read only
    "info/soft" 		: "Pyton example",
    "info/ver"		    : "0.0",
    "info/uptime"		: "?",

	## common instance info
    "node/name"		    : "Unnamed",
    "node/location"	    : "Nowhere",

	# items we want to send out.
	# remote configuration must tell
	# us which topics to use
    "topic/test"  	    : "test",
    "topic/ai0"  		: "unnamed_ai0",
    "topic/di0"	  	    : "unnamed_di0",

    "topic/pwm0"  	    : "unnamed_pwm0",
    
}

if __name__ == "__main__":
    print( "Will demonstrate remote config, now run tools/viewer and open remote config UI" )
    rcfg.init( init_items )
