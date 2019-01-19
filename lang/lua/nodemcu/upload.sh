#!/bin/sh

UP=./uploader.py

chmod a+rx $UP

$UP init.lua
$UP main.lua

$UP ../nodemcu/mybit.lua 
$UP ../nodemcu/mqtt_udp_defs.lua 
$UP ../nodemcu/mqtt_proto_lib.lua 
$UP ../nodemcu/mqtt_udp_lib_NodeMCU.lua 



