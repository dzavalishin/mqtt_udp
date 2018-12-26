#!/usr/bin/lua

local defs  = require "mqtt_udp_defs"
local mq = require "mqtt_udp_lib"

udp = mq.make_listen_socket()

while true do
    data, ip, port = udp:receivefrom()
    if data then
        print("Received: ", data, ip, port)
       --[[udp:sendto(data, ip, port)--]]
	topic,len = mq.parse_packet(data)
	print("Data: ",topic,len)
    end
    socket.sleep(0.01)
end



