--[[

  MQTT/UDP Lua library for NodeMCU. See mqtt_pub.lua and mqtt_sub.lua for usage.

  UNFINISHED!

  See also https://design.goeszen.com/how-to-receive-udp-data-on-nodemcu-lua-esp8266.html
  See also https://nodemcu.readthedocs.io/en/latest/en/modules/net/#netudpsocketlisten

]]

--local mqtt_udp_lib = {}
local mqtt_udp_lib = require "mqttudp.mqtt_proto_lib"

local defs  = require "mqttudp.mqtt_udp_defs"
--local socket = require "socket"
local bit = require "mqttudp.mybit"
local mcunet = require "net"

function mqtt_udp_lib.make_listen_socket()

    local udpSocket, our_ip

    tmr.alarm(1, 1000, 1, function()
        if wifi.sta.getip() == nil then
            print("Waiting for IP...")
        else
		tmr.stop(1)
		our_ip = wifi.sta.getip() -- we store our_ip, as we need it to setup udp socket properly
		print(" Your IP is ".. our_ip)
	end
    end)

    --udp = socket.udp()
    udpSocket = net.createUDPSocket() -- NodeMCU way
    --udp:setsockname("*", defs.MQTT_PORT )
    udpSocket:listen( defs.MQTT_PORT, our_ip ) -- NodeMCU way
    -- udp:settimeout(1)
    udp:settimeout()

    return udpSocket

end


function mqtt_udp_lib.make_publish_socket()

    --udp = socket.udp()
    local udp = net.createUDPSocket() -- NodeMCU way

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
    --socket:sendto( data, "255.255.255.255", defs.MQTT_PORT )
    socket:send(defs.MQTT_PORT, 0xFFFFFFFF, data ) -- NodeMCU way
end

function mqtt_udp_lib.recv_packet( socket )
    --data, ip, port = socket:receivefrom()
    return socket:receivefrom()
end


return mqtt_udp_lib


