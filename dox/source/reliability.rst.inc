
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

Actualy, simple test was made [#f1]_ to ckeck UDP reliability. One host in my house's local net was generating MQTT/UDP traffic as fast as 
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
