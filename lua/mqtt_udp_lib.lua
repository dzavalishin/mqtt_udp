local mqtt_udp_lib = {}

local defs  = require "mqtt_udp_defs"
local socket = require "socket"

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


function mqtt_udp_lib.parse_packet(pkt)

    print("pkt[0]: ", pkt[0] )

    --if( pkt[0] ~= 0x30 ) then
    if( pkt[0] ~= defs.PTYPE_PUBLISH ) then
        return "","";
    end

    total_len, pkt = mqtt_udp_lib.unpack_remaining_length(pkt);

    print("Total_len: ", total_len)

    topic_len = bit.bor( bit.band(pkt[1], 0xFF), bit.band(bit.lshift(pkt[0], 8), 0xFF) );
    topic = strsub( pkt, 2, topic_len+2 );
    value = strsub( pkt, topic_len+2 );
    
    --[[TODO use total_len--]]
    
    return topic,value
end



return mqtt_udp_lib