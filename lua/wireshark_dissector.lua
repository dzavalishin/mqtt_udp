-- local mq = require("mqtt_udp_lib")

packettypes  = { 
	[0x10] = "? Connect",  
	[0x20] = "? ConnAck",  
	[0x30] = "Publish",  
	[0x40] = "? PubAck",  
	[0x50] = "? PubRec",  
	[0x60] = "? PubRel",  
	[0x70] = "? PubComp",  
	[0x80] = "Subscribe",  
	[0x90] = "? SubAck",  
	[0xA0] = "? UnSubscribe",  
	[0xB0] = "? UnSubAck",  
	[0xC0] = "PingReq",
	[0xD0] = "PingResp",
	[0xE0] = "? Disconnect"
	}


mq_proto = Proto("MQTT.UDP", "MQTT/UDP protocol")
-- create a function to dissect it
function mq_proto.dissector(buffer,pinfo,tree)
    pinfo.cols.protocol = "MQTT/UDP"

    local type_str = packettypes[buffer(0,1):uint()]

    local subtree = tree:add(mq_proto,buffer(),"MQTT/UDP Data")

    subtree:add(buffer(0,1),"Packet type: " .. buffer(0,1):uint() .. " "..type_str)

    -- TODO upper byte
    local topic_len = buffer(3,1):uint()

    --subtree:add(buffer(3,1),"Topic len: " .. buffer(3,1):uint() )

    subtree:add(buffer(4,topic_len),"Topic: " .. buffer(4,topic_len):string() )

    subtree:add(buffer(4+topic_len),"Value: " .. buffer(4+topic_len):string() )


    -- total_len, pkt_rest = mq.unpack_remaining_length(buffer);
--    topic,val = mqtt_udp_lib.parse_packet(data)

-- the following is wrong!
--    subtree = subtree:add(buffer(1,1),"Packet length")
--    subtree:add(buffer(2,2),"Topic length: " .. buffer(2,1):uint())
--    subtree:add(buffer(3,1),"The 4th byte: " .. buffer(3,1):uint())
end
-- load the udp.port table
udp_table = DissectorTable.get("udp.port")
-- register our protocol to handle udp port 7777
udp_table:add(1883,mq_proto)
