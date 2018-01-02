#!/usr/bin/lua

local mqlib = require("mqtt_udp_lib")
local defs  = require("mqtt_udp_defs.lua")

local socket = require("socket")

udp = socket.udp()
udp:setsockname("*", defs.MQTT_PORT )
udp:settimeout(1)

while true do
    data, ip, port = udp:receivefrom()
    if data then
        print("Received: ", data, ip, port)
       --[[udp:sendto(data, ip, port)--]]
    end
    socket.sleep(0.01)
end



function parse_packet(pkt)

	--if( pkt[0] ~= 0x30 ) then
	if( pkt[0] ~= defs.PTYPE_PUBLISH ) then
		return "","";
	end

    total_len, pkt = mqlib.unpack_remaining_length(pkt);

    topic_len = bit.bor( bit.band(pkt[1], 0xFF), bit.band(bit.lshift(pkt[0], 8), 0xFF) );
    topic = strsub( pkt, 2, topic_len+2 );
    value = strsub( pkt, topic_len+2 );
    
    --[[TODO use total_len--]]
    
    return topic,value
end
