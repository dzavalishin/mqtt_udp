
Remote configuration
--------------------

MQTT/UDP can be used as a remote configuration protocol. There are two basic
modes of remote configuration: acive and passive. Active remote configuration mode
is needed when node to be configured has no local storage at all and has to
request all settings at each restart. Passive mode is good for nodes that keep
settings from start to start (in local FS or NVRAM) and configuration process is 
executed for them once or rarely.

.. _passive-rconfig:

Passive remote configuration
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Passive remote configuration is divided in three steps. First, when user
starts configurator GUI application, application sends (and repeats from time
to time) a SUBSCRIBE request. Second, any MQTT/UDP nodes that support remote
configuration see that request and answer with PUBLISH packet for all items
that can be set up. Last, configurator application sends back message with a new
item value.

.. figure:: diagrams/remote_configuration.*

   Passive remote configuration state diagram

   Basic remote configuration process. Configurator (which usually is a desktop
   application with GUI) sends SUBSCRIBE request, all nodes that support remote
   configuration answer with a list of items that can be configured. User sets 
   new value for an item and configurator updates it with PUBLISH message.

All the configurable items must have special topic name: ``$SYS/conf/{node-id}/{kind}/{name}``.
For example: ``$SYS/conf/024F55A20001/info/uptime``.

Node id
~~~~~~~

Node id is any string that will not change at least during one program run or
node start. If just one MQTT/UDP program is run on this hardware, you can use
net MAC address as id. Otherwise some GUID will do. Note that node-id can be
configurable item too!

.. index:: single: kind

Item kind
~~~~~~~~~

There are four predefined kinds:

``info``
   This kind is a readonly description which can not be configured
   but tells other side about us. There are ``info/soft`` - name
   of program, ``info/ver`` - version of program, ``info/uptime`` - 
   as is.

``node``
   This kind describes setting for program/device in general and is editable
   by user. Name and location are obvious items to be in this kind.
   Any other global (not related to specific function or input/output)
   setting can be added to this kind.

``topic``
   This kind is treated as configurable value that is a topic name
   used to send or receive data. Item name must be descriptive so
   that user can understand what it is about.

``net``
   Reserved for network settings.

You can use other kind strings as you wish. They will not be interpreted in
any way.


Item name
~~~~~~~~~

Just a descriptive name of an item, quite short but understandable by human.
In a later version there will be added long human readable description, but
in any case item name must be reasonable.

Predefined items
~~~~~~~~~~~~~~~~

Protocol implementations expect some item kind/name pairs to mean special thins.
Here is a list of know ones.

``info/soft``
   Readonly, name of software running in this node.

``info/ver``
   Readonly, version of software.

``info/uptime``
   Text string, uptime: "255d 02:12:34" or "5y 2m 13d 23:55:01"

``node/name``
   Name of this MQTT/UDP node. Human readable, not interpreted. "Weather sensors"
   or "A/C control"

``node/location``
   Where you can find it: "Kitchen", "building 12, 2nd floor, room 212", whatever.

``net/mac``
   Network MAC address, if not hardwired.

``net/ip``
   Network IP address, if static.

``net/mask``
   Netmask.

``net/router``
   Network default route.

Other network settings can be put to ``net`` kind.

Implementations
~~~~~~~~~~~~~~~

Configurator GUI tool exists as part of big Java MQTT/UDP viewer program,
see ``tools/viewer`` and ``build/mqtt_udp_view.*``. Clien implementations
examples are done for most project languages.

**Java**
    ``RemoteConfig`` class in ``ru.dz.mqtt_udp.config`` package.
    Simple example code is in ``main`` function of this class.

**Python**
    Example code is in ``lang/python3/examples/mqtt_udp_rconfig.py``.

**C**
    Simple example code is in ``lang/c/examples/rconfig.c``.
    More advanced embedded atmega microcontroller example 
    application is in a separate repository, see 
    https://github.com/dzavalishin/smart-home-devices/tree/master/mmnet_mqt_udp_server/main

**Lua**
    Lua example is up and running, but as Lua has no threads,
    adding this example to a real program will require quite
    a clever tailoring. See ``lang/lua/examples/mqtt_rconfig.lua``

Active remote configuration
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Active remote configuration is even simpler than passive one.
Starting node just requests in a loop all the items it needs 
to run. Configuration server replies with settings. Node
continues working when all critical data is received. Server
can proactively update settings later if some of them are
changed.

.. figure:: diagrams/remote_configuration_active.*

   Active remote configuration state diagram

Implementations
~~~~~~~~~~~~~~~

Currently this mode is implemented in Java as classes ``Requester`` and ``Provider``
(``ru.dz.mqtt_udp.config`` package) and server application ``tools/config_server``.
See also ``build/config_server.sh``.

