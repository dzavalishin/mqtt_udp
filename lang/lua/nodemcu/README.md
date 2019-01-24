# ModeMCU Lua MQTT/UDP implementation

This example is tested on Wemos D1 mini Pro. It shows both MQTT/UDP send
and listen use cases.

Files to be stored on NodeMCU unit from this directory:

*         init.lua - runs first, calls wifi.lua, sleeps 10 sec, runs main.lua
*         wifi.lua - sets up wifi client (ssid/passwd)
*         main.lua - main program loop

From ../mqttudp:

*         mqttudp/mybit.lua
*         mqttudp/mqtt_udp_defs.lua 
*         mqttudp/mqtt_proto_lib.lua 
*         mqttudp/mqtt_udp_lib_MCU.lua - NodeMCU specific UDP IO

Copy wifi.lua.in to wifi.lua and edit in correct WiFi network
SSDI and password. 

Copy main.lua.empty.in to main.lua.

Upload all the files above to device and reboot. It should sleep for 10 sec 
and start sending to test topic and listening to other devices/programs.

To test reception run some sending program on computer. For example, you
can run ../examples/mqtt_pub.lua or ../../python3/examples/random_to_udp.py 


## How do I put files to subfolder on NodeMCU

Sure, NodeMCU filesystem does not support subfolders.
But it also does not complain about '/' in a file name.
Upload all the needed files as is and run this snippet from ESPlorer:

```lua
function mvf( fn )
  nfn = "mqttudp/"..fn
  file.remove( nfn );
  file.rename( fn, nfn )
end

mvf("mybit.lua")
mvf("mqtt_udp_lib_MCU.lua")
mvf("mqtt_udp_defs.lua")
mvf("mqtt_proto_lib.lua")

```

It will do. Don't forget to upload all for files before running it.


## References

<https://nodemcu-build.com/>

<https://design.goeszen.com/how-to-receive-udp-data-on-nodemcu-lua-esp8266.html>

<https://nodemcu.readthedocs.io/en/latest/en/modules/net/#netudpsocketlisten>

<https://github.com/bkosciow/nodemcu_boilerplate>
