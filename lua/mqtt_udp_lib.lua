local mqtt_udp_lib = {}

local defs  = require "mqtt_udp_defs"
local socket = require "socket"
local bit = require "bit"

function mqtt_udp_lib.make_listen_socket()

    udp = socket.udp()
    udp:setsockname("*", defs.MQTT_PORT )
    -- udp:settimeout(1)
    udp:settimeout()

    return udp

end

function mqtt_udp_lib.unpack_remaining_length(pkt)
    remaining_length = 0
    while( 1 )
    do
        b = pkt:byte( 1 );
	--print("len byte: ", b )
        pkt = pkt:sub( 2 );
        remaining_length = bit.lshift( remaining_length, 7 );
        remaining_length = bit.bor( remaining_length, bit.band(b, 0x7F) );
        if( bit.band(b, 0x80) == 0)
        then
            break
        end
    end
    return remaining_length, pkt
end


function mqtt_udp_lib.parse_packet(pkt)

    --print("pkt[0]: ", pkt:byte( 1 ) )

    --if( pkt[0] ~= 0x30 ) then
    if( pkt:byte( 1 ) ~= defs.PTYPE_PUBLISH ) then
        return "","";
    end

    total_len, pkt = mqtt_udp_lib.unpack_remaining_length(pkt:sub(2));

    --print("Total_len: ", total_len);
    --print("pkt[0]: ", pkt:byte( 1 ) )

    topic_len = bit.bor( bit.band(pkt:byte(2), 0xFF), bit.band(bit.lshift(pkt:byte(1), 8), 0xFF) );
    topic = pkt:sub( 3, topic_len+2 );
    value = pkt:sub( topic_len+2+1 );
    
    --[[TODO use total_len--]]
    
    return topic,value
end



return mqtt_udp_lib