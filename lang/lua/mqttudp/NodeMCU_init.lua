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


wifi.setmode(wifi.STATIONAP)
cfg={}
cfg.ssid="???"
cfg.pwd="????"
wifi.sta.config(cfg)

