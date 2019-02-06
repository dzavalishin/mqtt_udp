--[[

 MQTT/UDP Lua library. See mqtt_pub.lua and mqtt_sub.lua for usage.

]]

local mq_lib = {}

mq_lib.defs  = require "mqttudp.mqtt_udp_defs"
local bit = require "mqttudp.mybit"


--local pubfd = mqtt_udp_lib.make_publish_socket()

local _pub_fd = nil


function pubfd()
    if _pub_fd == nil then
        _pub_fd = mq_lib.make_publish_socket()
    end
    return _pub_fd
end



------------------------------------------------------------------------------
--
-- Listen to packets 
--
------------------------------------------------------------------------------



--- Main internal callback to be called on raw data coming from socket
-- 
-- @param #bytse data Packet contents
-- @param #ip address ip Packet source address
-- @param #int port Packet source port
-- @param #function user_listener Sser's function to pass received and decoded packet to.
-- 
-- TODO prepare for mq_lib.udp_listen(proto_decoder,user_listener)
function mq_lib.proto_decoder(data, ip, port)
    ptype,topic,val = mq_lib.parse_packet(data)

    mq_lib.process_replies( ptype, topic, val, ip, port )
    --user_listener( ptype, topic, val, ip, port );
	mq_lib.user_listener( ptype, topic, val, ip, port );
end


--- User's entry point to listen to incoming messages
--
-- @param #void user_listener  function( ptype, topic, value, ip, port )
--

function mq_lib.listen( user_listener )
	mq_lib.user_listener = user_listener
    mq_lib.udp_listen()
end


function mq_lib.process_replies( ptype, topic, val, ip, port )
    -- respond to ping
    if ptype == "pingreq" then
        mq_lib.send_pingresp()
    end
    -- TODO repond to subscribe
end



------------------------------------------------------------------------------
--
-- Send packets
--
------------------------------------------------------------------------------



function mq_lib.send_publish( topic, value )
    data = mq_lib.make_publish_packet( topic, value )
    mq_lib.send_packet( pubfd(), data )
end

function mq_lib.send_subscribe( topic )
    data = mq_lib.make_subscribe_packet( topic )
    mq_lib.send_packet( pubfd(), data )
end




function mq_lib.send_pingresp()
    data = mq_lib.make_pingresp_packet()
    mq_lib.send_packet( pubfd(), data )
end

function mq_lib.send_pingreq()
    data = mq_lib.make_pingreq_packet()
    mq_lib.send_packet( pubfd(), data )
end


------------------------------------------------------------------------------
--
-- Parse packets
--
------------------------------------------------------------------------------







function mq_lib.unpack_remaining_length(pkt)
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


function mq_lib.parse_packet(pkt)

    --print("pkt[0]: ", pkt:byte( 1 ) )

    local ptype = bit.band( pkt:byte( 1 ), 0xF0 )
    local pflags = bit.band( pkt:byte( 1 ), 0x0F )

    --print( "ptype", ptype )

    if( ptype == mq_lib.defs.PTYPE_PINGREQ ) then
        return "pingreq","","";
    end

    if( ptype == mq_lib.defs.PTYPE_PINGRESP ) then
        return "pingresp","","";
    end

    if( ptype == mq_lib.defs.PTYPE_SUBSCRIBE ) then
        local total_len, pkt = mq_lib.unpack_remaining_length(pkt:sub(2));

        topic_len = bit.bor( bit.band(pkt:byte(2), 0xFF), bit.band(bit.lshift(pkt:byte(1), 8), 0xFF) );
        topic = pkt:sub( 3, topic_len+2 );
    
        --[[TODO use total_len--]]
    
        return "subscribe", topic, ""
    end

    if( ptype == mq_lib.defs.PTYPE_PUBLISH ) then
        local total_len, pkt = mq_lib.unpack_remaining_length(pkt:sub(2));

        topic_len = bit.bor( bit.band(pkt:byte(2), 0xFF), bit.band(bit.lshift(pkt:byte(1), 8), 0xFF) );
        topic = pkt:sub( 3, topic_len+2 );
        value = pkt:sub( topic_len+2+1, total_len+2 );
    
        --[[TODO use total_len--]]
    
        return "publish", topic, value
    end


    return "?","","";

end



------------------------------------------------------------------------------
--
-- Make packets
--
------------------------------------------------------------------------------


function mq_lib.make_publish_packet( topic, value )

    -- print("Topic: '"..topic.."' val '"..value.."'")

    pkt = "";
    pkt = pkt..string.char(mq_lib.defs.PTYPE_PUBLISH);

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


function mq_lib.make_subscribe_packet( topic )

    pkt = "";
    pkt = pkt..string.char(mq_lib.defs.PTYPE_SUBSCRIBE);

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


function mq_lib.make_pingresp_packet()

    pkt = "";
    pkt = pkt..string.char(mq_lib.defs.PTYPE_PINGRESP);
    pkt = pkt..string.char(0); -- payload length

    return pkt;
end


function mq_lib.make_pingreq_packet()

    pkt = "";
    pkt = pkt..string.char(mq_lib.defs.PTYPE_PINGREQ);
    pkt = pkt..string.char(0); -- payload length

    return pkt;
end


------------------------------------------------------------------------------
--
-- Topic match 
--
------------------------------------------------------------------------------



function mq_lib.match( tfilter, topicName )
		
    tc = 1;
    fc = 1;
    
    tlen = topicName:len()
    flen = tfilter:len()
    
    while true do
    
        -- begin of path part
        
        if tfilter:sub(fc,fc) == '+' then
    
            fc = fc + 1; -- eat +
            -- matches one path part, skip all up to / or end in topic
            while (tc <= tlen) and (topicName:sub(tc,tc) ~= '/') do
                tc = tc + 1; -- eat all non slash
            end

            -- now either both have /, or both at end
            
            -- both finished
            if (tc > tlen) and ( fc > flen ) then
                return true;
            end

            -- one finished, other not
            if (tc > tlen) ~= ( fc > flen ) then
                return false;
            end
            
            -- both continue
            if (topicName:sub(tc,tc) == '/') and (tfilter:sub(fc,fc) == '/') then
                tc = tc + 1;
                fc = fc + 1;
                -- continue; -- path part eaten
            else
                -- one of them is not '/' ?
                return false;
            end

        end
        
        -- TODO check it to be at end?
        -- we came to # in tfilter, done
        if tfilter:sub(fc,fc) == '#' then
            return true
        end
    
        -- check parts to be equal
        while true do
    
            -- both finished
            if (tc > tlen) and ( fc > flen ) then
                return true;
            end
    
            -- one finished
            if (tc > tlen) or ( fc > flen ) then
                return false;
            end

            -- both continue
            if (topicName:sub(tc,tc) == '/') and (tfilter:sub(fc,fc) == '/') then
                tc = tc + 1;
                fc = fc + 1;
                break; -- path part eaten
            end

            -- both continue
    
            if topicName:sub(tc,tc) ~= tfilter:sub(fc,fc) then
                return false;
            end

            -- continue
            tc = tc + 1;
            fc = fc + 1;
        end

    end -- while

end









------------------------------------------------------------------------------
--
-- End
--
------------------------------------------------------------------------------


return mq_lib













