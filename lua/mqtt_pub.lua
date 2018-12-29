#!/usr/bin/lua

-- Publish value to MQTT/UDP listeners

local mq = require "mqtt_udp_lib"

udp = mq.make_publish_socket()

topic = "Lua Sender Test";
val = "Hello";

print("Will send '"..topic.."'='"..val.."'");

while true do
    --data = mq.make_packet( topic, val );
    --mq.send_packet( data )
    mq.publish( udp, topic, val );
    print("Sent..");
    socket.sleep(2.01)
end



