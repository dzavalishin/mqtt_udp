


.. _lua-lang-api:

Lua Language API Reference
--------------------------

.. rem NOTE::

.. rem   Lua API is not final.

You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/lua repository.


Basic examples in Lua.

.. index:: single: send

Send data::


    local mq = require "mqttudp"
    mq.send_publish( topic, val );


.. index:: single: listen

Listen for data::


    local mq = require "mqttudp"
    
    local listener = function( ptype, topic, value, ip, port )
        print("'"..topic.."' = '"..val.."'".."	from: ", ip, port)
    end
    
    mq.listen( listener )


.. index:: single: send

Send packets
^^^^^^^^^^^^

There are functions to send different kinds of packets::

   local mq = require "mqttudp"

   mq.send_pingreq()
   mq.send_pingresp()
   mq.send_subscribe( topic )
   mq.send_publish( topic, value )


.. index:: single: match

Service
^^^^^^^

Match topic name against a pattern, processing `+` and `#` wildcards, returns ``true`` on match::

   local mu = require "mqttudp"
   local ok = mu.match( wildcard, topic_name )


NodeMCU
^^^^^^^

There is a version of Lua library for NodeMCU microcontroller firmware.
See ``lang/lua/nodemcu`` for examples.

