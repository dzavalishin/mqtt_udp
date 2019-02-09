
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

