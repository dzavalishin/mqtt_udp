# ModeMCU Lua MQTT/UDP implementation

Files to be stored on NodeMCU unit:

 *     init.lua - runs first, calls wifi.lua, sleeps 10 sec, runs main.lua
 *     wifi.lua - sets up wifi client (ssid/passwd)
 *     main.lua - main program loop
 *     mqttudp/mybit.lua
 *     mqttudp/mqtt_udp_defs.lua 
 *     mqttudp/mqtt_proto_lib.lua 
 *     mqttudp/mqtt_udp_lib_MCU.lua - NodeMCU specific UDP IO

## How do I put files to subfolder on NodeMCU

Sure, NodeMCU filesystem does not support subfolders.
But it also does not complain about '/' in a file name.
Run this snippet from ESPlorer:

```
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

<https://design.goeszen.com/how-to-receive-udp-data-on-nodemcu-lua-esp8266.html>

<https://nodemcu.readthedocs.io/en/latest/en/modules/net/#netudpsocketlisten>

<https://github.com/bkosciow/nodemcu_boilerplate>
