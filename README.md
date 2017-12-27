# mqtt_udp
Simpified version of MQTT over UDP

[See Russian version / Русская версия здесь](./README.ru.md)

MQTT is a cute simple protocol well suited for IoT and similar things.

But it can be even simpler and still be very usable: MQTT/UDP is
merely an MQTT Publish packets broadcast over an UDP.

MQTT/UDP is 

* extremely simple
* extremely fast, minimap possible latency
* excludes broker (which is single point of failure)
* lowers network traffic (each masurement is sent exactly once to all) 
* reasonably reliable (if we use it for sensors, which usually resend data every few seconds or so)
* can be supported even on a hardware which can not support TCP - in fact, only UDP send is required
* Zero configuration - a sensor node needs no setup, it just broadcasts its data.

For further reading:

* [MQTT topologies](./dox/Topologies.md)

This repository contains

* A simplest MQTT/UDP implementation in some popular programming languages.
* A simplest MQTT to MQTT/UDP bridge implementation in Pyton.
* A debug application to dump MQTT/UDP traffic (tools/viewer).

If you want to help a project, feel free to:

* Add implementation in your favorite programming language
* Write a bridge to classic MQTT protocol (we have very simple one here written in Python)
* Extend your favorite MQTT broker or IoT system (OpenHAB?) with MQTT/UDP support

It is really easy.

Reasons to avoid MQTT/UDP:

* You need to transfer long payloads. On some systems size of UDP datagram is limited.
* You need to know if datagram was delivered for sure. It is impossible with UDP.

Ways to extend MQTT/UDP:

* It seems to be reasonable to add some kind of signature to packets to prevent data spoofing. 
* Broadcast is not the best way to transmit data. Multicast is much better. Though multicast is not so well supported in IoT OS's or monitors.

