#!/usr/bin/lua

local mqlib = require("mqtt_udp_lib")

local socket = require("socket")

udp = socket.udp()
udp:setsockname("*", 1883)
udp:settimeout(1)

while true do
    data, ip, port = udp:receivefrom()
    if data then
        print("Received: ", data, ip, port)
       --[[udp:sendto(data, ip, port)--]]
    end
    socket.sleep(0.01)
end


function unpack_remaining_length(pkt)
    remaining_length = 0
    while( 1 )
	do
        pkt = strsub( pkt, 1 );
        b = pkt[0];
        remaining_length = bit.lshft( remaining_length, 7);
        remaining_length = bit.bor( remaining_length, bit.band(b, 0x7F) );
        if( bit.band(b, 0x80) == 0)
		then
            break
		end
    return remaining_length, pkt
	end
end

function parse_packet(pkt)

	if( pkt[0] ~= 0x30 ) 
	then
		return "","";
	end

    total_len, pkt = unpack_remaining_length(pkt);

    topic_len = bit.bor( bit.band(pkt[1], 0xFF), bit.band(bit.lshift(pkt[0], 8), 0xFF) );
    topic = strsub( pkt, 2, topic_len+2 );
    value = strsub( pkt, topic_len+2 );
    
    --[[TODO use total_len--]]
    
    return topic,value
end
