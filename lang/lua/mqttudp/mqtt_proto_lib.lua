--[[

 MQTT/UDP Lua library. See mqtt_pub.lua and mqtt_sub.lua for usage.

]]

local mqtt_proto_lib = {}

local defs  = require "mqtt_udp_defs"
local bit = require "bit"


--local pubfd = mqtt_udp_lib.make_publish_socket()

local _pub_fd = nil


function pubfd()
    if _pub_fd == nil then
        _pub_fd = mqtt_proto_lib.make_publish_socket()
    end
    return _pub_fd
end



function mqtt_proto_lib.listen( listener )
    local sock = mqtt_proto_lib.make_listen_socket()

    while true do
        --data, ip, port = sock:receivefrom()
        data, ip, port = mqtt_proto_lib.recv_packet( sock )
        if data then
            --print("Received: ", data, ip, port, type(data))
            --print("Received from: ", ip, port )
            --[[udp:sendto(data, ip, port)--]]
            ptype,topic,val = mqtt_proto_lib.parse_packet(data)
        --print( "ptype", ptype )
            listener( ptype, topic, val, ip, port );
        end
        socket.sleep(0.01)
    end

end


--[[
function mqtt_proto_lib.publish( socket, topic, value )
    data = mqtt_proto_lib.make_packet( topic, value )
    mqtt_proto_lib.send_packet( socket, data )
end
]]

function mqtt_proto_lib.publish( topic, value )
    data = mqtt_proto_lib.make_packet( topic, value )
    mqtt_proto_lib.send_packet( pubfd(), data )
end








function mqtt_proto_lib.unpack_remaining_length(pkt)
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


function mqtt_proto_lib.parse_packet(pkt)

    --print("pkt[0]: ", pkt:byte( 1 ) )

    local ptype = bit.band( pkt:byte( 1 ), 0xF0 )
    local pflags = bit.band( pkt:byte( 1 ), 0x0F )

    --print( "ptype", ptype )

    if( ptype == defs.PTYPE_PINGREQ ) then
        return "pingreq","","";
    end

    if( ptype == defs.PTYPE_PINGRESP ) then
        return "pingresp","","";
    end

    if( ptype == defs.PTYPE_SUBSCRIBE ) then
        local total_len, pkt = mqtt_proto_lib.unpack_remaining_length(pkt:sub(2));

        topic_len = bit.bor( bit.band(pkt:byte(2), 0xFF), bit.band(bit.lshift(pkt:byte(1), 8), 0xFF) );
        topic = pkt:sub( 3, topic_len+2 );
    
        --[[TODO use total_len--]]
    
        return "subscribe", topic, ""
    end

    if( ptype == defs.PTYPE_PUBLISH ) then
        local total_len, pkt = mqtt_proto_lib.unpack_remaining_length(pkt:sub(2));

        topic_len = bit.bor( bit.band(pkt:byte(2), 0xFF), bit.band(bit.lshift(pkt:byte(1), 8), 0xFF) );
        topic = pkt:sub( 3, topic_len+2 );
        value = pkt:sub( topic_len+2+1 );
    
        --[[TODO use total_len--]]
    
        return "publish", topic, value
    end


    return "?","","";

end



function mqtt_proto_lib.make_packet( topic, value )

    -- print("Topic: '"..topic.."' val '"..value.."'")

    pkt = "";
    pkt = pkt..string.char(defs.PTYPE_PUBLISH);

    tlen = topic:len()
    remaining_length = 2 + value:len() + tlen

    assert( remaining_length < 127 );

    pkt = pkt..string.char( bit.band( remaining_length, 0x7F ) );

    pkt = pkt..string.char( 0 ); -- upper byte of topic len = 0, can't be longer than 127
    pkt = pkt..string.char( tlen );

    pkt = pkt..topic;
    pkt = pkt..value;

    return pkt;
end



return mqtt_proto_lib
