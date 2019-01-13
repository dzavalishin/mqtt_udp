# MQTT/UDP protocol Java implementation

  ru.dz.mqtt_udp.util.Sub	- Listen example
  ru.dz.mqtt_udp.util.Pub	- Send example

All the rest is library.

See also ../../tools/viewer, simple visual MQTT/UDP traffic viewer written in Java.

## Build

I am building this with Eclipse. 

There is maven pom.xml bundled, so it can be built with mvn.

Code has no dependencies on Eclipse, so it can be built with any Java environment.

## Usage

**Send data:**

```java
PublishPacket pkt = new PublishPacket(topic, value);
pkt.send();

```

**Listen for data:**

```java
PacketSourceServer ss = new PacketSourceServer();
ss.setSink( pkt -> { 
    System.out.println("Got packet: "+pkt);

    if (p instanceof PublishPacket) {
        PublishPacket pp = (PublishPacket) p;			
    }

});

```
