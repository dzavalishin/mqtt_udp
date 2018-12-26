#!/usr/bin/lua

local defs  = require "mqtt_udp_defs"
local mq = require "mqtt_udp_lib"

udp = mq.make_listen_socket()

while true do
    data, ip, port = udp:receivefrom()
    if data then
        --print("Received: ", data, ip, port, type(data))
        print("Received from: ", ip, port )
       --[[udp:sendto(data, ip, port)--]]
	topic,val = mq.parse_packet(data)
	print("Topic: '"..topic.."'")
	print("Value: '"..val.."'")
    end
    socket.sleep(0.01)
end



