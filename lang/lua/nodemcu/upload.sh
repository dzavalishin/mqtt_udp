#!/bin/sh

UP=./uploader.py

chmod a+rx $UP

$UP init.lua
$UP main.lua

$UP ../mqttudp/mybit.lua 
$UP ../mqttudp/mqtt_udp_defs.lua 
$UP ../mqttudp/mqtt_proto_lib.lua 
$UP ../mqttudp/mqtt_udp_lib_MCU.lua 



