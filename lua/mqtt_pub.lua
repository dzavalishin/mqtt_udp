#!/usr/bin/lua

-- Publish value to MQTT/UDP listeners

local mq = require "mqtt_udp_lib"

udp = mq.make_publish_socket()

while true do
    topic = "Lua Sender Test";
    val = "Hello";
    --data = mq.make_packet( topic, val );
    --mq.send_packet( data )
    mq.publish( udp, topic, val );
    socket.sleep(2.01)
end



