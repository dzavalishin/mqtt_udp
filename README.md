# mqtt_udp
Simpified version of MQTT over UDP: Network is broker!

[See Russian version / Русская версия здесь](./README.ru.md)

MQTT is a cute simple protocol well suited for IoT and similar things.

But it can be even simpler and still be very usable: MQTT/UDP is
merely an MQTT Publish packets broadcast over an UDP.

MQTT/UDP is 

* Extremely simple
* Extremely fast, minimap possible latency
* Excludes broker (which is single point of failure)
* Lowers network traffic (each masurement is sent exactly once to all) 
* Reasonably reliable (if we use it for sensors, which usually resend data every few seconds or so)
* Can be supported even on a hardware which can not support TCP - in fact, only UDP send is required
* Zero configuration - a sensor node needs no setup, it just broadcasts its data.
* With some extension can be used on simplex channels and/or channels with native broadcast ability (radio,RS485)

For further reading:

* [MQTT/UDP Wiki](../../wiki)
* [MQTT/UDP Topologies](./dox/Topologies.md)

This repository contains

* A simplest MQTT/UDP implementation in some popular programming languages.
* A simplest MQTT to MQTT/UDP bridge implementation in Pyton.
* A debug application to dump MQTT/UDP traffic (tools/viewer).

If you want to help a project, feel free to:

* Add implementation in your favorite programming language
* Write a bridge to classic MQTT protocol (we have very simple one here written in Python)
* Extend your favorite MQTT broker or IoT system (OpenHAB?) with MQTT/UDP support
* Check MQTT/UDP specification/implementation against MQTT spec. We must be compatible where possible.

It is really easy.

Reasons to avoid MQTT/UDP:

* You need to transfer long payloads. On some systems size of UDP datagram is limited.
* You need to know if datagram was delivered for sure. It is impossible with UDP.

Ways to extend MQTT/UDP:

* It seems to be reasonable to add some kind of signature to packets to prevent data spoofing. 
* Broadcast is not the best way to transmit data. Multicast is much better. Though multicast is not so well supported in IoT OS's or monitors.

