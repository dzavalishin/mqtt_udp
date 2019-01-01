---
title: MQTT/UDP viewer tool
---

MQTT/UDP viewer is a tool which can help to see what's going on on the network with MQTT/UDP traffic. It shows:

* Current state (last data sent) for all the topics
* Scrolling list of packets that come along
* List of sending hosts

There's how it looks like:

![MQTT Viwer screenshot](https://raw.githubusercontent.com/dzavalishin/mqtt_udp/master/dox/TrafficViewerScreen_Dec2018.png)


For up to date info please visit viewer tool [help page](https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help) in Wiki.

Here we will describe it as it was at version 1.0.

As started Viewer displays all the PUBLISH packets traffic available at local host.

* You can write all data to log file

Use File/Log menu item or press Ctrl-L to open log file. Use File/Stop log menu to finish.

* You can stop/start update 

Press F5 or green arrow button at top left of the screen. Note that log is not updated too.


* Turn off pings

By default Viewer sends broadcast pings to discover MQTT/UDP nodes all over network.
You can turn it off with Send/Ping menu item.

