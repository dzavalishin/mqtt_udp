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

