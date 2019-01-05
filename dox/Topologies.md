# Possible topologies

Here is a list of more or less obvious use cases for MQTT/UDP

## Fault-tolerant sensors 

Some 2-4 temperature sensors are placed in one room and send
updates every 10 seconds or so. Update topic is the same for all the
sensors, so that every reader gets mix of all the readings.

Reader should calculate average for last 4-8 readings.

Result: reader gets average temperature in room and failure of
one or two sensors is not a problem at all.

Trying to build corresponding configuration with traditional MQTT or,
for example, Modbus you will have to:

* Setup broker
* Setup transport (topic names) for all separate sensors
* Setup some smart code which detects loss of updates from sensors
* Still calculate average
* Feed calculated average back if you want to share data with other system nodes

It is worth mentioning that up to 10% packet loss is not a problem for
such a setup (even if there is just one sensor), and in a real life if
LAN has 10% packet loss, TCP is nearly dead. So for regularly updated
values UDP is not really less reliable then TCP.

## One sensor, many listeners

IoT network is a lot of parties, operating together. It is usual that
many of them need one data source to make a decision. Just as an example,
my house control system consists of about 10 processing units of different
size. Mamy of them need to know if it is dark outside, to understand how
to control local lighting. Currently I have to distribute light sensor data
via two possible points of failure - controller it is connected to and
OpenHub software as a broker. I'm going to swithch to MQTT/UDP and feed
all the units directly.

## Multiple smart switches

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

## All the data is visible

That is a topology issue too. Broadcast/multicast nature of MQTT/UDP
lets you see what is going on on the "bus" exactly the same way as
all the parties see. There is a simple tool exist for that in this
repository, but you can use, for example well known WireShark as well.
