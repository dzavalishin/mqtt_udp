---
-- started from init.lua on NodeMCU, contains 
-- actual user's code - sensors, displays, etc
--

--package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install
package.path=package.path..";?"

local mq = require "mqttudp.mqtt_udp_lib_MCU"




local topic = "Lua Sender Test";
local val = "Hello";

print("Will send '"..topic.."'='"..val.."'");



main_timer = tmr.create()
main_timer:register( 1000, tmr.ALARM_AUTO,  
    function(t)
        --mq.send_publish( topic, val );
        print("Sent")
    end)

main_timer:start()
