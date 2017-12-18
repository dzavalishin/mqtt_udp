# mqtt_udp
Simpified version of MQTT over UDP

MQTT is a cute simple protocol well suited for IoT and similar things.

But it can be even simpler and still be very usable.

Broadcasting MQTT Publish packets with UDP is 

* extremely simple
* excludes broker (which is single point of failure)
* lowers network traffic (each masurement is sent exactly once to all) 
* reasonably reliable if we use it for sensors, which usually resend data every few seconds or so
* can be supported even on a hardware which can not support TCP - in fact, only UDP send is required

Here is a simplest MQTT/UDP implementation in some popular programming languages.

If you want to help a project, feel free to:

* Add implementation in your favorite language
* Write a bridge to classic MQTT
* Extend your favorite broker or IoT system with MQTT/UDP support

It is really easy.
