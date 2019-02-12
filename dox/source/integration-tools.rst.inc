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

This bridge is also can be used to integrate with cloud MQTT servers. Just set up corresponding host/port/login/password
in section ``[mqtt-gate]`` of ``mqtt-udp.ini`` and run ``mqtt_bidir_gate.py``.

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


