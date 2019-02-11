---
title: MQTT/UDP
---

# Hi and welcome to MQTT/UDP project

MQTT/UDP is as simple as possible protocol derived from, guess what, MQTT.
As name tells, MQTT/UDP using UDP broadcast as transport.

[GutHub repository](https://github.com/dzavalishin/mqtt_udp)

## MQTT/UDP is

* **Extremely simple**

There's Arduino sketch which is less than 3Kb. Though, full implementation is bigger,
but if needed, it can be extremely small.

* **Extremely fast, minimal possible latency**

Just one UDP packet delivers data to all the listeners at once. Usual latency is less than 0.5 msec on 100Mbps LAN.

* **Excludes broker (which is single point of failure)**

Home automation should not rely on system administrator to keep system working. It should just work. Any central
node will fail sooner or later. And MQTT/UDP does not need such node.

* **Lowers network traffic**

Each masurement is sent exactly once to all.

* **Quite reliable**

If we use it for sensors, which usually resend data every few seconds or so, sporadic packet loss is not a real problem.

And practical checks show that UDP packet loss in nowadays network is less than 0.5% even when traffic is high.

* **Can be supported even on a hardware which can not support TCP - in fact, only UDP send is required**

Arduino Nano with enc28j60 Ethnernet uint as sensor node? It is real and tested.

Look at [arduino example](https://github.com/dzavalishin/mqtt_udp/tree/master/lang/arduino).

* **Zero configuration - a sensor node needs no setup, it just broadcasts its data**

MQTT/UDP node does not need to connect to broker or any central auhority. Consequently, it needs no configuration,
such as server IP address.

* **Ither physiacl layers**

With some extension MQTT/UDP can be used on simplex channels and/or channels with native broadcast ability (radio,RS485).

This is not yet implemented, but seem to be really easy. And can be built on top of existing implementation.


## What is implemented

There's implementations of MQTT/UDP in:

* Java - additionally, GUI based protocol debug tool (viewer) is written in Java.
* Python 2.x (outdated)
* Python 3.x (3.6)
* Plain C
* Lua - though implementation is quite basic
* CodeSys 2.3 ST language (for PLC) - send only
* Arduino C - send only

There are debug tools and implementation examples too.


## I want to read more

* [Project Wiki](https://github.com/dzavalishin/mqtt_udp/wiki)
* [Use cases](https://github.com/dzavalishin/mqtt_udp/blob/master/dox/Topologies.md)

## How can I give it a try?

Fast start is described in [HOWTO](https://raw.githubusercontent.com/dzavalishin/mqtt_udp/master/HOWTO) file, please read.




## Feedback and bug reports

Here is an [Issue tracker](https://github.com/dzavalishin/mqtt_udp/issues).


## Blog

{% for post in site.posts %}
* **[{{ post.title }}]({{ site.url }}{{ post.url }})**
{% endfor %}

