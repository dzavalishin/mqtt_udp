--[[

  MQTT/UDP Lua library for NodeMCU. See mqtt_pub.lua and mqtt_sub.lua for usage.

  UNFINISHED!

  See also https://design.goeszen.com/how-to-receive-udp-data-on-nodemcu-lua-esp8266.html
  See also https://nodemcu.readthedocs.io/en/latest/en/modules/net/#netudpsocketlisten

]]

--local mqtt_udp_lib = {}
--local mqtt_udp_lib = require "mqttudp.mqtt_proto_lib"

--local defs  = require "mqttudp.mqtt_udp_defs"
--local socket = require "socket"
--local bit = require "mqttudp.mybit"
--local mcunet = require "net"


dofile("wifi.lua")

print("Wait for 10s");  -- let us reset if script is buggy

-- wait for network to be ready
net_timer = tmr.create()

-- Just wait 5 sec
wait_timer = tmr.create() 
wait_timer:register(10000, tmr.ALARM_SINGLE, 

    function (t) 

        print("Look for net");
        net_timer:start();

        t:unregister()
    end)

wait_timer:start()  


net_timer:register( 500, tmr.ALARM_AUTO,  
    function(t)
        if wifi.sta.getip() == nil then
            print("Waiting for IP...")
        else
            net_timer.stop(1)
            our_ip = wifi.sta.getip() -- we store our_ip, as we need it to setup udp socket properly
            print(" Your IP is ".. our_ip)
            dofile("main.lua") -- Start program itself
        end
    end)




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


