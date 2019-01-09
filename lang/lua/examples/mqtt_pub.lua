#!/usr/bin/lua

-- Publish value to MQTT/UDP listeners

package.path = "../mqttudp/?.lua;" .. package.path  -- let us test without lib install


local mq = require "mqtt_udp_lib"

local topic = "Lua Sender Test";
local val = "Hello";

print("Will send '"..topic.."'='"..val.."'");

while true do
    mq.send_publish( topic, val );
    print("Sent..");
    socket.sleep(2.01)
end



