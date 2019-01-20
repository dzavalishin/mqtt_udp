--[[

  MQTT/UDP Lua library for NodeMCU. See ../nodemcu/main.lua for usage.

  UNFINISHED!

]]

local mqtt_udp_lib = require "mqttudp.mqtt_proto_lib"

local defs  = require "mqttudp.mqtt_udp_defs"
local bit = require "mqttudp.mybit"
local mcunet = require "net"



function on_udp_in( socket, data, port, ip)
    mqtt_udp_lib.proto_decoder(data, ip, port)
end


--- Loop forever listening to incoming network data
-- @param #function proto_decoder Function to decode received data with
-- @param #function user_listener Function to call when packet is parsed
function mqtt_udp_lib.udp_listen()

    local sock, our_ip

    our_ip = wifi.sta.getip() -- we store our_ip, as we need it to setup udp socket properly

    sock = net.createUDPSocket()

	sock:on( "receive", on_udp_in )

    sock:listen( defs.MQTT_PORT, our_ip )
    --sock:listen( defs.MQTT_PORT )

    port, ip = sock:getaddr()
    print(string.format("UDP socket listening on %s:%d", ip, port))
    
end





function mqtt_udp_lib.make_publish_socket()

    --udp = socket.udp()
    local udp = net.createUDPSocket() -- NodeMCU way

    assert(udp)

    --assert(udp:settimeout(1)) ?

    --assert(udp:setoption('broadcast', true))
    --assert(udp:setoption('dontroute',true))


    --assert(udp:setsockname(s_address, defs.MQTT_PORT))
    --assert(udp:setsockname("*", defs.MQTT_PORT ))
   
    
    return udp

end



function mqtt_udp_lib.send_packet( socket, data )
    --socket:send(defs.MQTT_PORT, 0xFFFFFFFF, data ) -- NodeMCU way
    socket:send(defs.MQTT_PORT, wifi.sta.getbroadcast(), data ) -- NodeMCU way

end

function mqtt_udp_lib.recv_packet( socket )
    --data, ip, port = socket:receivefrom()
    return socket:receivefrom()
end


return mqtt_udp_lib


