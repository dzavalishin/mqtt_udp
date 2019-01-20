

function timer_sleep(sec)


sleep_timer = tmr.create() 
sleep_timer:register(sec*1000, tmr.ALARM_SINGLE, 

    function (t)

        print("Look for net");
        net_timer:start();

        t:unregister()
    end)

sleep_timer:start()


end




function mqtt_udp_lib.make_listen_socket()

    local udpSocket, our_ip

	our_ip = wifi.sta.getip() -- we store our_ip, as we need it to setup udp socket properly
	print("Listen on IP ".. our_ip)

    --udp = socket.udp()
    udpSocket = net.createUDPSocket() -- NodeMCU way
    --udp:setsockname("*", defs.MQTT_PORT )
    udpSocket:listen( defs.MQTT_PORT, our_ip ) -- NodeMCU way
    -- udp:settimeout(1)
    --udpSocket:settimeout() -- no method

    return udpSocket

end
