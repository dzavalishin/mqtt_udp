---
-- started from init.lua on NodeMCU, contains 
-- actual user's code - sensors, displays, etc
--



local mq = require "mqtt_udp_lib_NodeMCU.lua"




local topic = "Lua Sender Test";
local val = "Hello";

print("Will send '"..topic.."'='"..val.."'");

while true do
    mq.send_publish( topic, val );
    print("Sent..");
    timer_sleep(2)
end



