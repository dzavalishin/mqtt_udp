--[[

 MQTT/UDP Lua library. See mqtt_pub.lua and mqtt_sub.lua for usage.

]]

--local mqtt_udp_lib = {}
local mqtt_udp_lib = require "mqttudp.mqtt_proto_lib"

local defs  = require "mqttudp.mqtt_udp_defs"
local socket = require "socket"
local bit = require "mqttudp.mybit"

function mqtt_udp_lib.make_listen_socket()

    local udp = socket.udp()
    --print(assert(c:setoption("reuseport", true)))
    --print("reuseport: ", udp:setoption("reuseport", true))
    udp:setoption("reuseaddr", true)
    udp:setsockname("*", defs.MQTT_PORT )
    -- udp:settimeout(1)
    udp:settimeout()

    return udp

end


function mqtt_udp_lib.make_publish_socket()

    local udp = socket.udp()

    assert(udp)
    --assert(udp:settimeout(1))
    assert(udp:settimeout())
    assert(udp:setoption('broadcast', true))
    assert(udp:setoption('dontroute',true))
    --assert(udp:setsockname(s_address, defs.MQTT_PORT))
    --assert(udp:setsockname("*", defs.MQTT_PORT ))

    return udp

end



function mqtt_udp_lib.send_packet( socket, data )
    socket:sendto( data, "255.255.255.255", defs.MQTT_PORT )
end

function mqtt_udp_lib.recv_packet( socket )
    --data, ip, port = socket:receivefrom()
    return socket:receivefrom()
end


-- TODO use me in listen


function mqtt_udp_lib.udp_listen(proto_decoder,user_listener)

    local sock = mqtt_udp_lib.make_listen_socket()

    while true do

        data, ip, port = mqtt_udp_lib.recv_packet( sock )
        if data then
            proto_decoder(data, ip, port, user_listener)
        end
        socket.sleep(0.01)
    end

end



return mqtt_udp_lib


