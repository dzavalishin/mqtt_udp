--[[

 MQTT/UDP Lua library. See mqtt_pub.lua and mqtt_sub.lua for usage.

]]

local mqtt_proto_lib = {}

local defs  = require "mqttudp.mqtt_udp_defs"
local bit = require "mqttudp.mybit"


--local pubfd = mqtt_udp_lib.make_publish_socket()

local _pub_fd = nil


function pubfd()
    if _pub_fd == nil then
        _pub_fd = mqtt_proto_lib.make_publish_socket()
    end
    return _pub_fd
end



------------------------------------------------------------------------------
--
-- Listen to packets - TODO - move loop to UDP code because of NodeMCU
--
------------------------------------------------------------------------------

--[[
function mqtt_proto_lib.listen( listener )
    local sock = mqtt_proto_lib.make_listen_socket()

    while true do
        --data, ip, port = sock:receivefrom()
        data, ip, port = mqtt_proto_lib.recv_packet( sock )
        if data then
            --print("Received: ", data, ip, port, type(data))
            --print("Received from: ", ip, port )
            --udp:sendto(data, ip, port)--
            ptype,topic,val = mqtt_proto_lib.parse_packet(data)

            mqtt_proto_lib.process_replies( ptype, topic, val, ip, port )
            listener( ptype, topic, val, ip, port );
        end
        socket.sleep(0.01)
    end

end ]]





-- TODO prepare for mqtt_proto_lib.udp_listen(proto_decoder,user_listener)
function mqtt_proto_lib.proto_decoder(data, ip, port, user_listener)
    ptype,topic,val = mqtt_proto_lib.parse_packet(data)

    mqtt_proto_lib.process_replies( ptype, topic, val, ip, port )
    user_listener( ptype, topic, val, ip, port );
end


function mqtt_proto_lib.listen( user_listener )
    mqtt_proto_lib.udp_listen( mqtt_proto_lib.proto_decoder, user_listener)
end


function mqtt_proto_lib.process_replies( ptype, topic, val, ip, port )
    -- respond to ping
    if ptype == "pingreq" then
        mqtt_proto_lib.send_pingresp()
    end
    -- TODO repond to subscribe
end



------------------------------------------------------------------------------
--
-- Send packets
--
------------------------------------------------------------------------------



function mqtt_proto_lib.send_publish( topic, value )
    data = mqtt_proto_lib.make_publish_packet( topic, value )
    mqtt_proto_lib.send_packet( pubfd(), data )
end

function mqtt_proto_lib.send_subscribe( topic )
    data = mqtt_proto_lib.make_subscribe_packet( topic )
    mqtt_proto_lib.send_packet( pubfd(), data )
end




function mqtt_proto_lib.send_pingresp()
    data = mqtt_proto_lib.make_pingresp_packet()
    mqtt_proto_lib.send_packet( pubfd(), data )
end

function mqtt_proto_lib.send_pingreq()
    data = mqtt_proto_lib.make_pingreq_packet()
    mqtt_proto_lib.send_packet( pubfd(), data )
end


------------------------------------------------------------------------------
--
-- Make / parse packets
--
------------------------------------------------------------------------------







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



function mqtt_proto_lib.make_publish_packet( topic, value )

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


function mqtt_proto_lib.make_subscribe_packet( topic )

    pkt = "";
    pkt = pkt..string.char(defs.PTYPE_SUBSCRIBE);

    tlen = topic:len()
    remaining_length = 2 + tlen + 1

    assert( remaining_length < 127 );

    pkt = pkt..string.char( bit.band( remaining_length, 0x7F ) );

    pkt = pkt..string.char( 0 ); -- upper byte of topic len = 0, can't be longer than 127
    pkt = pkt..string.char( tlen );

    pkt = pkt..topic;

    pkt = pkt..string.char(0); -- QuS byte

    return pkt;
end


function mqtt_proto_lib.make_pingresp_packet()

    pkt = "";
    pkt = pkt..string.char(defs.PTYPE_PINGRESP);
    pkt = pkt..string.char(0); -- payload length

    return pkt;
end


function mqtt_proto_lib.make_pingreq_packet()

    pkt = "";
    pkt = pkt..string.char(defs.PTYPE_PINGREQ);
    pkt = pkt..string.char(0); -- payload length

    return pkt;
end



return mqtt_proto_lib













