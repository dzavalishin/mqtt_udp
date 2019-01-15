# MQTT/UDP implemented in Lua

## Library

mqtt_udp_lib.lua		- MQTT/UDP protocol Lua library entry point, using classic LUA socket IO

mqtt_udp_lib_NodeMCU.lua - MQTT/UDP protocol Lua library entry point, using NodeMCU socket IO - **INCOMPLETE**

mqtt_proto_lib.lua      - part of library, protocol itself

mqtt_udp_defs.lua		- part of library, generated outside

## Examples

mqtt_pub.lua			- command line tool that uses Lua library to publish data with MQTT/UDP protocol

mqtt_sub.lua			- command line tool that uses Lua library to listen to MQTT/UDP messages

## Install

luarocks install mqttudp



## Usage

**Send data:**


```lua
local mq = require "mqtt_udp_lib"
mq.send_publish( topic, val );

```

**Listen for data:**


```lua
local mq = require "mqtt_udp_lib"

local listener = function( ptype, topic, value, ip, port )
    print("'"..topic.."' = '"..val.."'".."	from: ", ip, port)
end

mq.listen( listener )
```


## Wireshark

wireshark_mqtt_dissector.lua - incomplete WireShark protocol dissolver to be able to dump MQTT/UDP packets
