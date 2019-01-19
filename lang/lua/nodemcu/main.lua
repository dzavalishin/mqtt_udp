---
-- started from init.lua on NodeMCU, contains 
-- actual user's code - sensors, displays, etc
--

--package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install
package.path=package.path..";?"

local mq = require "mqtt_udp_lib_MCU"




local topic = "Lua Sender Test";
local val = "Hello";

print("Will send '"..topic.."'='"..val.."'");

while true do
    mq.send_publish( topic, val );
    print("Sent..");
    timer_sleep(2)
end



