
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



.. include:: introduction.rst.inc

.. include:: topologies.rst.inc

.. include:: reliability.rst.inc

.. include:: rconfig.rst.inc 



Packets and general logic
=========================

.. include:: protocol-packets-logic.rst.inc


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



.. include:: integration-tools.rst.inc




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

.. include:: network.rst.inc 

.. include:: work-in-progress.rst.inc 

.. include:: faq-links.rst.inc 








