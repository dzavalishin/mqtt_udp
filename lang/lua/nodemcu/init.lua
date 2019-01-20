
package.path = package.path..";?"

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
            t:unregister()
            dofile("main.lua") -- Start program itself
        end
    end)


