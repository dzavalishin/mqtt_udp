
.. _python-lang-api:

Python Language API Reference
-----------------------------

As you already guessed, python implementation is native too. You can browse 
sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/python3 
repository.
There is also lang/python directory, which is for older 2.x python environment, 
but it is outdated. Sorry, can't afford to support it. If you need python 2.x, 
you can backport some python3 code, it should be quite easy.


Let's begin with examples, as usual.

.. index:: single: send

Send data::


    mqttudp.engine.send_publish( "test_topic", "Hello, world!" )


.. index:: single: listen

Listen for data::


    def recv_packet(pkt):
        if pkt.ptype != me.PacketType.Publish:
            print( str(pkt.ptype) + ", " + pkt.topic + "\t\t" + str(pkt.addr) )
            return
        print( pkt.topic+"="+pkt.value+ "\t\t" + str(pkt.addr) )
       
    mqttudp.engine.listen(recv_packet)


Module mqttudp.engine
^^^^^^^^^^^^^^^^^^^^^

Main package, implements MQTT/UDP protocol.

Paccket class::

    class PacketType(Enum):
        Unknown     = 0
        Publish     = 0x30
        Subscribe   = 0x80
        PingReq     = 0xC0
        PingResp    = 0xD0


    class Packet(object):
        def __init__( self, ptype, topic, value, pflags, ttrs ):
            self.ptype  = ptype
            self.pflags = pflags
            self.topic  = topic
            self.value  = value
            self.ttrs   = ttrs
            self.addr   = None


Functions:

* ``send_ping()`` - send PINGREQ packet.
* ``send_ping_responce()`` - send PINGRESP packet. It is sent automatically, you don't have to.
* ``listen(callback)`` - listen for incoming packets.
* ``send_publish( topic, payload)`` - this what is mostly used.
* ``send_subscribe(topic)`` - ask other party to send corresponding item again. This is optional.
* ``set_muted(mode: bool)`` - turn off protocol replies. Use for send-only daemons which do not need to be discovered.


.. index:: single: match

Match topic name against a pattern, processing `+` and `#` wildcards, returns True on match::

   import mqttudp.engine as me
   me.match("aaa/+/bbb", "aaa/ccc/bbb")

Turn of automatic protocol replies::

   set_muted(mode: bool)

.. index:: single: throttle

Set minimum time between packets sent, msec::

   set_throttle(msec: int)

Set network address to listen at (choose incoming packets network interface). Address must 
be equal to address of some network interface::

   set_bind_address( "192.168.1.1" )

Set network address to broadcast to (choose outgoing packets network interface). Address must
be broadcast address for some of existing network interfaces. Ask local network administrator
if unsure::

   set_broadcast_address( "192.168.1.255" )

.. index:: single: OpenHAB

.. _python-ini-file:

Module mqttudp.config
^^^^^^^^^^^^^^^^^^^^^

Additional module, sets up configuration file reader. Most command line utilities use it to get settings.
It reads ``mqtt-udp.ini`` file in current directory. Here is an example::


    [DEFAULT]
    host = smart.
    
    [mqtt-gate]          # Settings for MQTT to MQTT/UDP gate
    login = 
    password = 
    
    subscribe=#
    #host = smart.       # See [DEFAULT] above
    
    #blacklist=/topic    # Regexp to check if topic is forbidden to relay
    #blacklist=/openhab
    
    [openhab-gate]
    #port=8080           # There's builtin default
    #host = smart.       # Settings for MQTT/UDP to OpehHAB gate
    
    #blacklist=/topic    # Regexp to check if topic is forbidden to relay

    # which sitemap to use for reading data from openhab
    #sitemap=default

Usage::

    import mqttudp.config as cfg
    
    cfg.setGroup('mqtt-gate')           # set ours .ini file [section]
    
    blackList=cfg.get('blacklist')      # read setting


.. index:: single: Remote config
.. _python-rconfig-api:

Module mqttudp.rconfig
^^^^^^^^^^^^^^^^^^^^^^

Additional module implemening passive remote configuration client
(party that is being configured) implementation.

There is a complete demonstration example exist.

To see example working please run ``mqtt_udp_rconfig.py`` first
and mqtt_udp_view after it. In viewer please press middle toolbar button
to open remote configuration window. This window will show all 
running MQTT/UDP instances that can be configured. There must be 
``Python test node`` among them. Select its tab. You will see all
the configurable items (from ``init_items`` dictionary) as text
fields. Meanwhile ``mqtt_udp_rconfig.py`` will be sending a random
number with "test" topic. Enter new topic name in a field near 
"topic: test" description and press nearest button to send new 
setting to program. Notice that now it sends random data with a topic
you just set up.

Now lets look at example code (see examples/mqtt_udp_rconfig.py)::

    import mqttudp.rconfig as rcfg

    init_items = {
        ## read only
        "info/soft"         : "Pyton example",
        "info/ver"          : "0.0",
        "info/uptime"       : "?",

    	## common instance info
        "node/name"         : "Unnamed",
        "node/location"	    : "Nowhere",

    	# items we want to send out
        "topic/test"  	    : "test",
        "topic/ai0"         : "unnamed_ai0",
        "topic/di0"         : "unnamed_di0",

        "topic/pwm0"        : "unnamed_pwm0",    
    }

    def send_thread():
        while True:
            n = str(random.randint(0, 9))
            print( "Send "+n )
            rcfg.publish_for( "test", n )
            time.sleep(2)
    

    if __name__ == "__main__":
        print( "Will demonstrate remote config" )
        rcfg.init( init_items )

        st = threading.Thread(target=send_thread, args=())
        st.start()

        mq.listen( rcfg.recv_packet )

This example shows how to use remote configuration subsystem. Dictionary
``init_items`` contains list of items which can possiblly be configured
remotely. Different elements are used in a different ways.

In general each item in a list is a configurable thing. For example,
``"node/location" : "Nowhere"`` is item which name is ``node/location``
and initial value is ``Nowhere``. (It is supposed as a memo for user
to know where an appliance or computer running this code is installed.)
Another example is ``"topic/ai0" : "unnamed_ai0"`` - it is supposed
to be a configurable topic name that device uses to send data from 
some analogue input. User must configure topic name and it will be
used by node to send data.

Generally item keys consist of two parts
separated with slash: ``"topic/pwm0"``, ``"info/uptime"`` or 
``"node/name"``. Left part is named **kind** and defines the way item 
is processed. Here is a list of known kinds.

``info``
   Read only description of node/program.

``node``
   General node information or settings. 

``topic``
   This kind is a configurable value that is a topic name.

``net``
   Reserved for network settings.

Please see more on kinds in :ref:`passive-rconfig`.

Lets go on with code. Line ``rcfg.init( init_items )`` sets subsystem
up. Remote config subsystem first loads previous settings from .ini file
(you can set file name with ``set_ini_file_name(fn)`` function) and
fills all items from given ``init_items`` dictionary that was not read
from .ini file. Items with ``info`` kind are taken from ``init_items``
in any way.

After init your program continues working, but must call ``recv_packet``
function of ``mqttudp.rconfig`` for each incoming MQTT/UDP packet for
remote configuration to work.

There are three ways to use configured parameters.

Just read parameter
   You can just call ``get_setting( name )`` for needed item to get current
   configured value. For example, ``get_setting( "net/mac" )`` or, say,
   ``lcd_print( get_setting( "node/location" ))``. If your node will be
   reconfigured in run time, on next call there will be new value.

Send data for configurable topic
   By calling ``publish_for( item_of_topic, data )`` you will send data
   to a topic which is configured by item with, guess what, ``topic`` kind.
   See above ``rcfg.publish_for( "test", n )`` - this line looks up config
   item named ``topic/test``, and uses its value as a topic to publisn 
   value of varible ``n`` to. 

Check incoming packet topic
    On receiving incoming PUBLISH packet, you can use ``is_for( topic_item, topic )``
    function, which checks ``topic`` parameter to be equal to value of config
    item named ``"topic/"+topic_item``, such as ``is_for( "pwm0", topic )`` will
    return ``True`` if ``topic`` variable contains string equal to value of config
    item ``"topic/pwm0"``.

Only thing left to mention is that you can set callback with call to ``set_on_config( callback )``
and it will be called if remote configuration happens. Config item name and
new value will be passed as parameters.




Module mqttudp.interlock
^^^^^^^^^^^^^^^^^^^^^^^^

Additional module, implements two classes: ``Bidirectional`` and ``Timer``.

``Bidirectional`` is used by bidiractional gateways to prevent loop traffic::

    # Init interlock object which will
    # forbid reverse direction traffic
    # for 5 seconds after message passed
    # in one direction.
    
    ilock = mqttudp.interlock.Bidirectional(5) 

    # Check if we can pass forward

    if ilock.broker_to_udp(msg.topic, msg.payload):
        mqttudp.engine.send_publish( msg.topic, msg.payload )
        print("To UDP: "+msg.topic+"="+str(msg.payload))
    else:
        print("BLOCKED to UDP: "+msg.topic+"="+str(msg.payload))

    # and back

    if ilock.udp_to_broker(topic, value):
        bclient.publish(topic, value, qos=0)
        print( "From UDP: "+topic+"="+value )
    else:
        print( "BLOCKED from UDP: "+topic+"="+value )

Value is not actually used in current implementation. It is passed
for later and smarter versions.

``Timer`` prevents updates from coming too frequently::

    it = mqttudp.interlock.Timer(10)

    if it.can_pass( topic, value ):
        print("From broker "+topic+" "+value)
        mqttudp.engine.send_publish( topic, value )
    else:
        print("From broker REPEAT BLOCKED "+topic+" "+value)


It checks if value is changed. Such values are permitted to pass through.
Unchanged ones will pass only if time (10 seconds in this example) is passed
since previous item come through.


.. module:: mqttudp.mqtt_udp_defs

Module mqttudp.mqtt_udp_defs
^^^^^^^^^^^^^^^^^^^^^^^^^^^^



This module is not for user code, it is used internally. But you can get library release version from it::

    PACKAGE_VERSION_MAJOR = 0
    PACKAGE_VERSION_MINOR = 4






