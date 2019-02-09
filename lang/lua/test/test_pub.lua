#!/usr/bin/lua 

-- Publish value to MQTT/UDP listeners

--package.path = "../mqttudp/?.lua;" .. package.path  -- let us test without lib install
package.path = "../?/init.lua;../?.lua" .. package.path  -- let us test without lib install


--local mq = require "mqtt_udp_lib"
local mq = require "mqttudp"

local topic = arg[1];
local val = arg[2];

print("Will send '"..topic.."'='"..val.."'");

mq.send_publish( topic, val );

print("Sent ok");
