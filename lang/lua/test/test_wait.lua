#!/usr/bin/lua

package.path = "../?/init.lua;../?.lua;" .. package.path  -- let us test without lib install

local mq = require "mqttudp"
local os = require "os"

local need_topic = arg[1];
local need_value = arg[2];


local listener = function( ptype, topic, value, ip, port )
    if ptype == "publish" then
        --print("'"..topic.."' = '"..val.."'".."	from: ", ip, port)
        if( (topic == need_topic) and (value == need_value) ) then
        print("Got it!")
        os.exit(0)
        end
    end
end



print("Will listen for MQTT/UDP packets and wait for "..need_topic.." = "..need_value);
mq.listen( listener )

