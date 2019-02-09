

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

Additional repositories
^^^^^^^^^^^^^^^^^^^^^^^

These projects use or extend MQTT/UDP.

CCU825 connector, Java
    https://github.com/dzavalishin/ccu825modbus

AtMega128 IO Unit
    https://github.com/dzavalishin/smart-home-devices/tree/master/mmnet_mqt_udp_server



