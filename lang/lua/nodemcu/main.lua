---
-- started from init.lua on NodeMCU, contains 
-- actual user's code - sensors, displays, etc
--

package.path=package.path..";?"

local mq = require "mqttudp.mqtt_udp_lib_MCU"



local topic = "NodeMCU Sender Test";
local val = "Hello";

print("Will send '"..topic.."'='"..val.."'");


main_timer = tmr.create()
main_timer:register( 1000, tmr.ALARM_AUTO,  
    function(t)
        mq.send_publish( topic, val );
        print("Sent")
    end)

main_timer:start()


local listener = function( ptype, topic, value, ip, port )
    if ptype == "publish" then
        print("'"..topic.."' = '"..value.."'".."	from: ", ip, port)
    else
        print(ptype.." '"..topic.."' 	from: ", ip, port)
    end
end


mq.listen( listener )


