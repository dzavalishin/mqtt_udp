#!/usr/bin/lua


--package.path = package.path .. ";../mqttudp" -- let us test without lib install
--package.path = "../mqttudp;" .. package.path  -- let us test without lib install
package.path = "../mqttudp/?.lua;" .. package.path  -- let us test without lib install

--local defs  = require "mqtt_udp_defs"
local mq = require "mqtt_udp_lib"

local listener = function( ptype, topic, value, ip, port )
    --print("Topic: '"..topic.."'")
    --print("Value: '"..val.."'")
    --print("From: ", ip, port )
    if ptype == "publish" then
        print("'"..topic.."' = '"..val.."'".."	from: ", ip, port)
    else
        print(ptype.." '"..topic.."' 	from: ", ip, port)
    end
end

print("Will listen for MQTT/UDP packets");

--local udp = mq.make_listen_socket()
mq.listen( listener )

--[[
while true do
    data, ip, port = udp:receivefrom()
    if data then
        --print("Received: ", data, ip, port, type(data))
        print("Received from: ", ip, port )
	topic,val = mq.parse_packet(data)
	print("Topic: '"..topic.."'")
	print("Value: '"..val.."'")
    end
    socket.sleep(0.01)
end
]]


