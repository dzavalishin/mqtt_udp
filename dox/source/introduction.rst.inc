Introduction
------------


MQTT/UDP is a simplest possible protocol for IoT, smart home applications and 
robotics. As you can guess from its name, it is based on MQTT (which is quite 
simple too), but based on UDP and needs no broker.

.. sidebar:: *Network is a broker*

   Your network does most of the broker's work.

Fast track for impatient readers: MQTT/UDP native implementations exist in Java, 
Python, C, Lua and PLC specific ST language. See corresponding references:

* :ref:`c-lang-api`
* :ref:`java-lang-api`
* :ref:`python-lang-api`
* :ref:`lua-lang-api`
* :ref:`st-lang-api`

If you want to test MQTT/UDP on a real hardware, take a look at :ref:`sketches` 
part. Ready made software is described in :ref:`integration` part.

Now some words on MQTT/UDP idea. It is quite simple. Broker is a `single point 
of failure <https://en.wikipedia.org/wiki/Single_point_of_failure>`_ and can be avoided. Actual
traffic of smart home installation is not too big and comes over a separated (by firewall) 
network. There are many listeners that need same data, such as:

.. index:: single: OpenHAB

* main UI subsystem (such as OpenHAB installation)
* special function controllers (light, climate units)
* per-room or per-function controllers (kitchen ventilation, bath room sensors, room CO2 sensors, etc)
* in-room displays (room and outdoor temperature)

All these points generate some information (local sensors, state) and need 
some other information. By the way, CAN bus/protocol is made for quite the same requirements,
but is not good for TCP/IP and Ethernet. Actually, to some extent, MQTT/UDP is CAN for Ethernet.

So, MQTT/UDP is sending data with UDP broadcast. It means that every message 
is simuloneusly sent to all possible recipients with just one network packet.

Every listener selects packets it wants to listen to and processes them as it wishes.

As a result, minmal MQTT/UDP implementation is extremely simple. Though, there are more
options exist which are described later.

Main use cases for MQTT/UDP are covered below.


Data exchange
^^^^^^^^^^^^^

Main and, for most applicartions, the only use case. It is really simple. Sender transmits one
PUBLISH packet per message. Packet contains topic name (such as "rooms/dinner/temperature") and
value. Value can be text string or binary data, but most programs will wait for text as packet value.

As there is no broker, parties do not need such things as CONNNECT, SUBSCRIBE or anything else but
PUBLISH message.

All the MQTT/UDP programs on the network will receive message and decide if they need it.


Reliable exchange
^^^^^^^^^^^^^^^^^

Sender transmits PUBLISH message with non-zero QoS field. Receiver replies with
PUBACK packet. If no acknowledge received, sender re-sends message.

Current libraries do not support this scenario out of the box, but it can be implemented by user
code. Later versions of libraries will have this case implemented.


Data request
^^^^^^^^^^^^

There is request-reply scenario possible. Requesting party sends SUBSCRIBE message, one that is
responcible for requested topic replies with PUBLISH message.

This scenario can be used for remote configuration use case: configuration daemon keeps set of topics 
and configuration settings per topic, but does not send them to not to spam network with rarely
needed data. Some IoT device turns on and requests topics that contain needed configuration parameters,
gets needed settings and continues working.

If configuration settings are changed, config server re-publishes corresponding topics to update
device settings.

Please see Java ``config.Provider`` and ``config.Requester`` classes for further info.

There is also a ready simple remote configuration server in ``tools/config_server``.

Topic request
^^^^^^^^^^^^^

There is reverse scenario possible. Remote configuration program can send SUBSCRIBE message for
topic, that is a wildcard for all possible configuration topics for device or all devices.
Devices should respond back with PUBLISH messages for all the configurable items.

See :ref:`passive-rconfig` for more details.

Discovery
^^^^^^^^^

Party that needs to find who is on the network sends PINGREQ request. All the others reply with
PINGRESP messages, and requester builds a map of all active MQTT/UDP hosts on the network.

