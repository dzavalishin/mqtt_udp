

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


