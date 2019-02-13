
Differences from MQTT
---------------------

MQTT/UDP is based on classic MQTT, but differs a bit. Frist of all, just a subset of
packet types used and, of course, as there is no broker there is no need for CONNECT,
CONNACK or DISCONNECT.

.. figure:: illustrations/PacketStructure.*
   :align: right

   General MQTT/UDP packet structure

Additionally, MQTT/UDP does not send or expect variable header (packet ID field)
present in some MQTT packets.

Current implementation also ignores packet flags completely, but it will change later.

Most implementations support Tagged Tail Records addition to the protocol, which
extends and replaces variable header in an extencible and flexible way.

Please read `description at project Wiki <https://github.com/dzavalishin/mqtt_udp/wiki/Tagged-Tail>`_.


Packet types and use
--------------------

**PUBLISH**
   It is extremely simple to use MQTT/UDP. Basic use case is: one party sends **PUBLISH** packets,
   other receives, selecting for itself ones with topics it needs. That is all. No connect,
   no subscribe, no broker address to configure - we're broadcasting.

   For most applications it is all that you need. But there are 3 other packet types that possibly can 
   be used.

**SUBSCRIBE**
   MQTT/UDP uses this as a request to resend some topic value. It is not automated in any way by library code (but will be),
   so you have to respond to such a packet manually, if you want. It is intended for remote configuration use to let configuration
   program to request settings values from nodes. This is partially implemented.

**PINGREQ**
   Ping request, ask all nodes to reply. This is for remote configuration also, it helps config program to detect all nodes on the network.
   Library code automatically replies to ``PINGREQ`` with ``PINGRESP``.

**PINGRESP**
   Reply to ping. You don't need to send it manually. It is done automatically.

It is supposed to use **PUBACK** packet later to support reliable delivery.


Topic names
-----------

One important thing about topics is **$SYS** topic. MQTT/UDP is a broadcast environment, so each node which wants to use **$SYS**
must distinguish itself by adding MAC address or other id as a subtopic under **$SYS**: **$SYS/{group}/02AF03E6235C**. Topic name 
**$SYS/conf/{host-id}** is to be used for configurable parameters.

.. rem TODO list of parameters

One more special thing I'm going to use is **$META** topic name suffix. It will possibly be used to request/send topic metadata.
For example, if we have **kitchen/temperature** topic, then **kitchen/temperature/$META/name** can be used to pass printable
topic name, and **kitchen/temperature/$META/unit** - to send measuring unit name.

