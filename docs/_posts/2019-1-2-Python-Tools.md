---
title: Python tools
---

As you already know, we 
have [GUI tool](https://dzavalishin.github.io/mqtt_udp/2019/01/01/Viewer-Tool.html) for 
monitoring MQTT/UDP data transfer and current items state.

But if you're working with distant node by ssh/telnet it does not help much
on the remote side.

Thus, there are also some simple command line tools writen in Python language.
We will refer to [lang/python3/examples](https://github.com/dzavalishin/mqtt_udp/tree/master/lang/python3/examples) directory,
but most of them exist in [lang/python/examples](https://github.com/dzavalishin/mqtt_udp/tree/master/lang/python/examples) too.


# Random data sender

Simple random_to_udp.py script sends random numbers over and over.
Nice for first tests and for firewall checks. Run GUI traffic viewer
on centarl node and random_to_udp.py on other node to see if data comes through.

# Traffic dump tools

There are two:

* dump.py just dumps all PUBLISH packets that come
* listen.py displays only changed topic values

Can be used to check connectivity too.

# Kill network with traffic tools

**Use with caution!**

Sender - seq_storm_send.py - will generate packets as fast as possible. Each
packet will have value which is incremented sequentially.


Check tool - seq_storm_check.py  - will listen to packets, check if some packets 
are lost or out of order, and calculate speed and packets loss rate.


I'll mention again that running seq_storm_send.py for a long time will
bring network to death. Do this tests for a short time and when no one
will complain.

# Gateway tools

These are really simple programs to integrate MQTT/UDP to other IoT systems.

* mqtt_udp_to_broker.py will copy all MQTT/UDP traffic to traditional MQTT broker.

* mqtt_broker_to_udp.py will copy all traffic from MQTT to MQTT/UDP.

* And bidirectional_gate.py integrates MQTT/UDP and MQTT in both ways.

It has simple loop prevention engine inside which stops same PUBLISH packet to come
in reverse direction in 5 seconds after it comes forward.

* mqtt_udp_to_openhab.py will copy all MQTT/UDP traffic to OpenHAB.

Of course we need bidirectional gateway for OpenHAB too.

# Ping tool

Simple ping.py will send ping packet and dump replies. Used to test ping responce.







If you need some of these tools in python2.x or other language, 
[make us know](https://github.com/dzavalishin/mqtt_udp/issues) or just
port yourself, you're welcome to help project.
