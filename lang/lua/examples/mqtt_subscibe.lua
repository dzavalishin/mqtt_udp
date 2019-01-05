#!/usr/bin/lua

-- Send subscribe packet to MQTT/UDP listeners

--package.path = package.path .. ";../mqttudp" -- let us test without lib install
package.path = "../mqttudp/?.lua;" .. package.path  -- let us test without lib install


local mq = require "mqtt_udp_lib"


local topic = "Lua Sender Test";

print("Will send subscribe to '"..topic.."'");

mq.subscribe( topic );



