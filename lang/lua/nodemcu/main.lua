---
-- started from init.lua on NodeMCU, contains 
-- actual user's code - sensors, displays, etc
--

package.path=package.path..";?"

local mq = require "mqttudp.mqtt_udp_lib_MCU"

local led_pin = 4

local topic = "NodeMCU/Sender/Test";
local val = "Hello";


--local sda, scl = 6, 5
local sda, scl = 2, 1
i2c.setup(0, sda, scl, i2c.SLOW) 
si7021.setup()

--hum, temp, hum_dec, temp_dec = si7021.read()
-- Float firmware using this example
--print("Humidity: "..hum.."\n".."Temperature: "..temp)


print("Will send '"..topic.."'='"..val.."'");


gpio.mode(led_pin, gpio.OUTPUT)
        

function blink()
    gpio.write(led_pin, gpio.LOW)
    tmr.alarm(0, 50, 1, function()
        gpio.write(led_pin, gpio.HIGH)
    end)
end

main_timer = tmr.create()
main_timer:register( 1000, tmr.ALARM_AUTO,  
    function(t)
        --print("Read sensor")
        hum, temp, hum_dec, temp_dec = si7021.read()

        print(string.format("Humidity:\t\t%d.%03d\nTemperature:\t%d.%03d\n", hum, hum_dec, temp, temp_dec))
        mq.send_publish( "Wemos/humid", string.format("%d.%03d", hum, hum_dec ));
        mq.send_publish( "Wemos/temp", string.format("%d.%03d", temp, temp_dec ));
        --print("Humidity: "..hum.."\n".."Temperature: "..temp)
        --mq.send_publish( "Wemos/humid", tostring(hum) );
        --mq.send_publish( "Wemos/temp", tostring(temp) );
        --mq.send_publish( topic, val );
        blink()
        --print("Sent")
        
    end)

main_timer:start()


local listener = function( ptype, topic, value, ip, port )
    blink()
    if ptype == "publish" then
        --print("'"..topic.."' = '"..value.."'".."	from: ", ip, port)
    else
        --print(ptype.." '"..topic.."' 	from: ", ip, port)
    end    
end


mq.listen( listener )


