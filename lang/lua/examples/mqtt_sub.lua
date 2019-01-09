#!/usr/bin/lua

package.path = "../mqttudp/?.lua;" .. package.path  -- let us test without lib install

local mq = require "mqtt_udp_lib"

local listener = function( ptype, topic, value, ip, port )
    if ptype == "publish" then
        print("'"..topic.."' = '"..val.."'".."	from: ", ip, port)
    else
        print(ptype.." '"..topic.."' 	from: ", ip, port)
    end
end

print("Will listen for MQTT/UDP packets");

mq.listen( listener )

