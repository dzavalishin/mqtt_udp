local mqlib = require("mqtt_udp_lib")

-- trivial protocol example
-- declare our protocol
trivial_proto = Proto("trivial","MQTT/UDP")
-- create a function to dissect it
function trivial_proto.dissector(buffer,pinfo,tree)
    pinfo.cols.protocol = "TRIVIAL"
    local subtree = tree:add(trivial_proto,buffer(),"MQTT/UDP Data")
    subtree:add(buffer(0,1),"Packet type: " .. buffer(0,1):uint())
-- the following is wrong!
--    subtree = subtree:add(buffer(1,1),"Packet length")
--    subtree:add(buffer(2,2),"Topic length: " .. buffer(2,1):uint())
--    subtree:add(buffer(3,1),"The 4th byte: " .. buffer(3,1):uint())
end
-- load the udp.port table
udp_table = DissectorTable.get("udp.port")
-- register our protocol to handle udp port 7777
udp_table:add(1883,trivial_proto)
