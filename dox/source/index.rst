
.. toctree::
   :maxdepth: 3
.. rem    :caption: Contents:


Welcome to MQTT/UDP
===================

.. only:: html

   Package version |version|
   
   You can get this document in `PDF format`_.

.. _PDF format: https://media.readthedocs.org/pdf/mqtt-udp/latest/mqtt-udp.pdf

.. rem Indices and tables
.. rem ------------------

.. rem .. only:: html

.. rem   * :ref:`genindex`
.. rem   * :ref:`modindex`
.. rem   * :ref:`search`



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


Possible topologies
-------------------

Here is a list of more or less obvious scenarios for MQTT/UDP

Fault-tolerant sensors 
^^^^^^^^^^^^^^^^^^^^^^

Some 2-4 temperature sensors are placed in one room and send
updates every 10 seconds or so. Update topic is the same for all the
sensors, so that every reader gets mix of all the readings.

Reader should calculate average for last 4-8 readings.

Result: reader gets average temperature in room and failure of
one or two sensors is not a problem at all.

Trying to build corresponding configuration with traditional MQTT or,
for example, Modbus you will have to:

*   Setup broker
*   Setup transport (topic names) for all separate sensors
*   Setup some smart code which detects loss of updates from sensors
*   Still calculate average
*   Feed calculated average back if you want to share data with other system nodes

.. figure:: illustrations/AveragedSensors.*

   Typical MQTT/UDP use case.

   This diagram shows three sensors duplicating each other. For example,
   three outer temperature sensors. Wall display, history database and
   main smarthome unit get copy of all data from sensors. Malfunction of
   any unit does not make any problemn for others.

One sensor, many listeners
^^^^^^^^^^^^^^^^^^^^^^^^^^

IoT network is a lot of parties operating together. It is usual that
many of them need one data source to make a decision. Just as an example,
my house control system consists of about 10 processing units of different
size. Many of them need to know if it is dark outside, to understand how
to control local lighting. Currently I have to distribute light sensor data
via two possible points of failure - controller it is connected to and
OpenHub software as a broker. I'm going to swithch to MQTT/UDP and feed
all the units directly.


.. index:: single: UDP

Multiple smart switches
^^^^^^^^^^^^^^^^^^^^^^^

Some wall switches are controlling the same device. All of them send
and read one topic which translates on/off state for the device.

Of course, if one switch changes the state, all others read the state broadcast
and note it, so that next time each switch knows, which state it should
switch to.

It is possible, of course, that UDP packet from some switch will be lost.
So when you switch it, nothing happens. What do you do in such a situation?
Turn switch again, of course, until it works!

In this example I wanted to illustrate that even in this situation UDP
transport is not really that bad.



Roadside processor
^^^^^^^^^^^^^^^^^^

Data processors such as triggers, unit converters, calculators of
different kinds can be easily implemented with MQTT/UDP as standalone
script or a small program that just listens for required incoming data,
performs calculations and sends results back to MQTT/UDP.

Other script or IoT/SmartHome component can then use resulting data.

There is an example of such combination in MQTT/UDP repository. Java
program in ``tools/tray`` is setting up desktop tray informer which 
displays some MQTT/UDP parameters if user clicks on tray icon.

Companion script ``lang/python3/examples/trigger.py`` is listening to
some topic and if topic value is out of range sends information on
``tray/message`` topic with a worning. Tray program listens for that
topic and displays a warning to user on reception of such a message.

.. rem TODO draw an image



System debugging
^^^^^^^^^^^^^^^^

Broadcast/multicast nature of MQTT/UDP
lets you see what is going on on the "bus" exactly the same way as
all the parties see. There is a simple tool exist for that in this
repository, but you can use, for example well known WireShark as well.



.. index:: single: UDP

Reliability
-----------

.. NOTE::

   There's QoS support for MQTT/UDP is in development, which makes it as reliable as TCP version.

As MQTT/UDP is based on UDP protocol, which does not guarantee packet delivery, one can suppose that MQTT/UDP is not reliable. Is it?

Not at all.

If we use it for repeated updates, such as sensor data transfer, UDP is actually more reliable, than TCP! Really. If our network drops each
second packet, TCP connection will be effectively dead, attempting to resend again and again outdated packets which are not needed anymore.
And MQTT/UDP will just loose half of readings, which is not really a problem for 99% of installations. So, TCP helps just if packet loss rate
is quite low.

Actualy, I made simple test [#f1]_ to ckeck UDP reliability. One host in my house's local net was generating MQTT/UDP traffic as fast as 
possible and other checked packets to be sequent, counting speed and error rate. Two IPTV units was started to show HD content and one 
of the computers was copying some few GBytes to file server. Result was quite surprising: MQTT/UDP error rate grew to... 0.4% with about 50K
packets/second, but TV sets stopped showing, being, obviusly, starved.

Anyway, I'm going to add completely reliable mode to MQTT/UDP in near future.

.. rem TODO some graph of packet loss rate?

.. rubric:: Footnotes

.. [#f1] Corresponding tools are in repository and you can run such test yourself.


.. index:: single: throttle

Speed limit
^^^^^^^^^^^

There is one more reliability issue exist when we use UDP. Low power microcontrollers
are quite slow and their ability to receive lots of UDP packets per second are limited.
There is possible packet loss due to low processing power of some slow nodes, not because
of network delivery is not reliable.

That's why protocol implementations include throttling subsystem, which limits amount of
packets sent per time interval.

By default it is tuned for maximum of 10 packets per second. Java and Python implementations
use millisecond timing and send max of 3 packets with no speed limit, and then add 300 msec
pause. C implementation currently uses 1 second time granularity and lets application send
up to 10 packets with no limit and then waits for a second.

Actual tests of reception speed capability were done with Wemos D1 Mini unit programmed
with MQTT/UDP Lua implementation.

There is ``set_throttle``/``setThrottle`` function in all languages but Lua, which lets you set 
speed limit according to your hardware capabilities, or disable it at all by setting to 0.

.. include:: rconfig.rst.inc 


Packets and general logic
=========================

Differences from MQTT
---------------------

MQTT/UDP is based on classic MQTT, but differs a bit. Frist of all, just a subset of
packet types used and, of course, as there is no broker there is no need for CONNECT,
CONNACK or DISCONNECT.

Additionally, MQTT/UDP does not send or expect variable header (packet ID field)
present in some MQTT packets.

Current implementation also ignores packet flags completely, but it will change later.

Currently in the development an Tagged Tail Records addition to the protocol, which
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



API Reference
=============

MQTT/UDP is implemented in five languages, but implementations differ. Most 
complete and fast developing are Java and Python versions. Others
follow a bit later. Please see `map of languages and features <https://github.com/dzavalishin/mqtt_udp/wiki/Features-and-languages-map>`_ on a project Wiki.

.. include:: api-ref-c.rst.inc 

.. include:: api-ref-java.rst.inc 

.. include:: api-ref-python.rst.inc

.. include:: api-ref-lua.rst.inc

.. include:: api-ref-codesys.rst.inc





.. _integration:

Integration and tools
=====================


Connectors
----------

Project includes two simple connectors. One joins MQTT/UDP with classical MQTT, other connects to OpenHAB.

All the tools read ``mqtt-udp.ini`` file, see :ref:`python-ini-file` for detailed description. You have, at 
least, to set host name for both tools.

Classic MQTT
^^^^^^^^^^^^

It is obvious that MQTT/UDP can be used together with traditional MQTT, so there's a simple gateway to 
pass traffic back and forth. It is written in Python and copies everything from one side to another and
back. There's interlock logic introduced that prevents loops by not passing same topic message in reverse 
direction for some 5 seconds.

To run connector go to ``lang/python3/examples`` directory and start ``mqtt_bidir_gate.py`` program.

There are also unidirectional gates ``mqtt_broker_to_udp.py`` and ``mqtt_udp_to_broker.py``.

There is an example of service configuration file ``mqttudpgate.service`` for Unix ``systemctl`` service control tools.

.. rem TODO ``systemctl enable mqttudpgate.service``? autostart, start/stop/status


.. index:: single: OpenHAB

OpenHAB
^^^^^^^

At the moment there are two one way gateways, from MQTT/UDP to OpenHAB and back, and one complete bidirectional gateway.

To run connector go to ``lang/python3/examples`` directory and start ``mqtt_udp_to_openhab.py``,
``openhab_to_udp.py``, or ``openhab_bidir_gate.py`` program.

Minimal configuration required is to set OpenHAB host name in section ``[openhab-gate]`` of ``mqtt-udp.ini`` file.
Gateway uses OpenHAB sitemap to get list of items to read. By default it uses sitemap named ``default``. If your
OpenHAB setup most populated sitemap is not default one, please set sitemap name in .ini file too.

.. index:: single: CCU825

CCU825 GSM Controller
^^^^^^^^^^^^^^^^^^^^^

There is a connector for a CCU825 controller in a `separate repository <https://github.com/dzavalishin/ccu825modbus>`_.


Programs
--------

There are some programs and scripts made to help testing MQTT/UDP library. Some of them are written in C and Java
but most exist just in Python version.


C programs
^^^^^^^^^^

* **mqtt_udp_clock** - sends date and time value to network once a minute. Can be used to set
  clock in IoT/smarthome peripheral devices. NB! Use SNTP if you need high accuracy. 


Java programs
^^^^^^^^^^^^^

* **tools/config_server** - simple remote configuration server. See corresponding README for details.


Python programs
^^^^^^^^^^^^^^^

* **random_to_udp.py** - send random numbers with 2 sec interval, to test reception.
* **dump.py** - just show all traffic.
* **ping.py** - send ping and show responces. By using set_muted(mode: bool) function it turns off protocol replies so it will not resond to itself.
* **subscribe.py** - send subscribe request.
* **seq_storm_send.py** - send sequential data with no speed limit (use -s to set limit, though).
* **seq_storm_check.py** - check traffic sent by *seq_storm_send.py* and calculate speed and error rate.



Traffic viewer
--------------

A GUI tool to view what's going on and send data too.

.. figure:: ../TrafficViewerScreen_Jan2019.png

   Screenshot of MQTT/UDP viewer tool (Windows)

It is supposed that this tool can be used as remote configuration for MQTT/UDP nodes on the network.

To run program go to project root directory and start ``mqtt_udp_view.cmd`` or ``mqtt_udp_view`` depending on your OS. You will
need Java 8 and JavaFX installed for it to run. Please download it from http://java.com or
try to use OpenJDK. (I did not yet.)

Actual user guide is at project Wiki: https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help

.. figure:: illustrations/MqttUdpViewer_MacOS.png

   Screenshot of MQTT/UDP viewer tool (Mac OS)

   Being written in Java viewer works on Mac OS. It also must run on other
   operating systems with Java, but it was not yet tested.

To run viewer you will need MqttUdpViewer.jar - on any OS ``java -jar MqttUdpViewer.jar`` will start
program. For Windows there is MqttUdpViewer.exe which is a starter for MqttUdpViewer.jar,
so in widows you can start it with ``MqttUdpViewer`` command.

For details please read `wiki <https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help>`_, but in short,
viewer has following parts:

Value view
^^^^^^^^^^

Top list displays current value of all topics that was transmitted since 
program start. There is also time of last update.


Log view
^^^^^^^^

Shows each message passing. You can choose if you will see ping/reply packets or no.


Host list
^^^^^^^^^

Displays list of network hosts sending MQTT/UDP traffic.


Message Editor
^^^^^^^^^^^^^^

Can be used to send messages to network. It is possible to send message just to one host
or broadcast them. It is also possible to send **SUBSCRIBE** messages to request 
topic data to be sent.

.. index:: single: Remote config

Remote configurator
^^^^^^^^^^^^^^^^^^^

Remote configuration is described in :ref:`passive-rconfig` in detail. This
program implements passive remote configuration mode.

.. figure:: ../TrafficViewerScreen_Feb2019.png 

   Screenshot of MQTT/UDP viewer tool remote configuration window

Programs or devices that use MQTT/UDP passive remote configuration feature can be
configured by network with this tool. Instances provide list of configurable items
and each tab of config window lets you set instance parameters. There is a complete
example made for Python, see :ref:`python-rconfig-api` for description. Implementations
of client side also made for C, Java and Lua languages.



.. index:: single: OpenHAB

System Tray Informer
--------------------

There is a simple program that adds an icon to a system tray. This icon lets you see some data from MQTT/UDP or control one
OpenHAB item. Being a Java program it should run on MacOS and Linux, but it was not tested with Linux yet. 
Illustrations show how it looks in Windows and Mac OS.

.. rem It is shown on a :ref:`WinTrayRight` and :ref:`WinTrayLeft` figures like it looks in Windows. :ref:`MacTrayMouseOver`


.. _WinTrayRight:

.. figure:: illustrations/TrayInfo.png 

   Windows: tray icon informer

   This informer is shown when you press right mouse button.
   It shows two items defined in .ini file, see reference.
   In this example mains voltage and total power consumption
   are shown.

.. _WinTrayLeft:

.. figure:: illustrations/TrayMenu.png 

   Windows: tray icon menu

   Menu is shown when right mouse button is pressed.




Setting up
^^^^^^^^^^

This program reads an ``mqttudptray.ini`` configuration file on start::


   topic1=PLK0_activePa
   topic2=PLK0_Va
   
   topic1header=Power consumption
   topic2header=Mains Voltage
   
   # experimental
   #
   controltopic=GroupGuestMain
   
You can define which two topics will be displayed, and what human readable names they have.
The ``controltopic`` setting is for controlling light (or other ON/OFF switch) via
OpenHAB. If defined, *Light on* and *Light off* menu items of a tray icon will send ON and OFF
values to corresponding topic.

Current version of MQTT/UDP does not support QoS, and, possiblly on/off message can be lost.
That is why this function is marked as experimental.

Running
^^^^^^^

In any OS you will need ``MqttUdpTray.jar`` and ``mqttudptray.ini``. There is ``MqttUdpTray.exe`` 
for windows. In other systems (with Java 8 installed) please execute ``javaw -jar MqttUdpTray.jar``
or ``java -jar MqttUdpTray.jar`` command. All the files are in the ``build`` directory.


.. _MacTrayMenu:

.. figure:: illustrations/MacOSMenu.png 

   Tray icon menu

   Menu is shown when left mouse button is pressed.


.. _MacTrayMouseOver:

.. figure:: illustrations/MacOSMouseOver.png 

   Tray icon on mouse over

   Tooltip is shown when mouse is over the icon.





Addendums
=========

.. rem Installation
.. rem ------------
.. rem luarocks install mqttudp
.. rem pypi ??
.. rem maven


Cook Book
---------

Even if you think that MQTT/UDP is not for you and can't be used as primary transport in your project, there are
other possibilities to use it together with traditional IoT infrastructure

Displays
^^^^^^^^

Send a copy of all the items state to MQTT/UDP and use it to bring data to hardware and software displays. For example, this
project includes an example program (``tools/tray`` directory, see figure :ref:`MacTrayMouseOver` ) to display some MQTT/UDP items via an icon in a desktop
tray. Being a Java program it should work in Windows, MacOS and Unix.

.. rem TODO screenshot



.. index:: single: OpenHAB

Sensors and integrations
^^^^^^^^^^^^^^^^^^^^^^^^

It is not really easy to write a native Java connector for OpenHAB. Write it in Python for MQTT/UDP and 
translate data from MQTT/UDP to OpenHAB. It is really easy.

By the way, there is quite a lot of sensors drivers in Python for Raspberry and clones.

Don't like Raspberry? Use Arduino or some ARM CPU unit and C version of MQTT/UDP.

.. rem TODO example project code!


.. rem NB!! Need very stable and fast bidir gateway for that. It is not now.
.. rem Scripting
.. rem ^^^^^^^^^

.. rem Writing (and debugging!) a script for OpenHAB is not an easy task. Setting up bidirectional 
.. rem gate between MQTT/UDP and OpenHAB enables you to write scripts on MQTT/UDP side.



.. rem main bus
.. rem desktop programs and debug



.. _sketches:


Sketches
--------

There are more or less complete demo implementations exist.

Wemos D1 Mini Pro
^^^^^^^^^^^^^^^^^

This sketh must also run on any NodeMCU hardware.

See lang/lua/nodemcu for source code and instruction.


Arduino
^^^^^^^

This sketch must run on any Arduino device as long as it has ENC28J60 ethernet module connected.

See lang/arduino for source code and instructions for this one.


Network
-------

.. NOTE::

   Basic digital signature subsystem for MQTT/UDP is in development now. Java implementation is already
   supporting it, contact us if you want to test it or take part in development. 
   See ``ru.dz.mqtt_udp.Engine`` class.

Current implementation of MQTT/UDP has no security support. It is supposed that later some
kind of packet digital signature will be added. At the moment I suppose that protocol can
be used in comletely secure networks or for not really important data.

.. figure:: illustrations/IdealHomeNet.*

   Ideal structure of network.

   Segment for a smart home is separated from local network for usual computers. MQTT/UDP
   data can be forwarded there on firewall, but not backwards.

Actually I personally use MQTT/UDP in typical home network, separated from Internet with NAT but with 
no separation between smart home and other computers. I do think that would my home network be hacked
into, intervention into the smart home system is the lesser of possible evils.


Work in progress
----------------

There are parts of protocol or additional components design that 
not finished completely, and are subject of discussion.

QoS Server
^^^^^^^^^^

Nowadays UDP is quite reliable (typical loss rate is < 0.5% even in a busy network),
but not 100% reliable. Things are easy in point to point communications. Send a message,
wait for acknowledgement, resend if none received in a reasonable time. But
MQTT/UDP is broadcast protocol. Do we have to wait for ack from all nodes in a network?
Some nodes? Which ones?

It is makes sense that we can build a map of nodes that listen to us by collecting
their responces. But we want to keep MQTT/UDP implementation simple and this is not
that simple. And not any node needs such high a reliability.

The idea is to add separate server on a network that will build lists of listeners
for each topic, collect low-QoS ack packets and sent one high-QoS ack packet
to topic publisher(s).

.. figure:: diagrams/qos_server.*

    QoS server sequence diagram

Note that such server is not a single point of failure. First of all, there can be more
than one instance of QoS server. Second, even if QoS server fails, nodes continue to
send data. Though, each packet will be resent few times, but it is not a communications
failure. Last, but not least, sending node can stop resending after few acks with lower
QoS. For example, sending node can take for acknowledge one QoS 3 ack message and 2 or 3
QoS 2 ones.

FAQ
---

**Q:** There's MQTT-SN, aren't you repeating it?

**A:** 
   MQTT-SN still needs broker. And MQTT/UDP still simpler. :)


**Q:** Why such a set of languages?

**A:** 
   **C** is for embedded use. I want it to be easy to build smert sensor or wall display/control unit
   based on MQTT/UDP.
   
   **Python** is for gateways and scripting. Writing small command line program or daemon in Python is easy.
   Also, there is a lot of Python drivers for various sensors and displays on Raspberry/Orange/Banana/whatever PI.
   
   **Java** is for serious programming and GUI apps. Viewer was easy thing to do with JavaFX.
   
   **Lua** is for NodeMCU and, possibly, other embedded platforms.
   
   **CodeSys** is evil you can't escape.
   


.. rem Building all
.. rem ------------

.. rem First of all, you don't have to make all. Most things are made for you. See build directory.

.. rem Threre are two tested environments for complete build: Windows Cygwin and Linux.

.. rem Requirements:
.. rem * binutils, make, gcc, python 3.6+, lua 5.1+
.. rem * java jdk 1.8 or later, maven, JUnit 4, launch4j

Links
-----

GitHUb: https://github.com/dzavalishin/mqtt_udp

Error reports and feature requests: https://github.com/dzavalishin/mqtt_udp/issues

If you use MQTT/UDP, please let me know by adding issue at GitHub. :)










