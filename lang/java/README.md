# MQTT/UDP protocol Java implementation


  ru.dz.mqtt_udp.util.Sub	- Listen example
  ru.dz.mqtt_udp.util.Pub	- Send example

All the rest is library.

See also ../../tools/viewer, simple visual MQTT/UDP traffic viewer written in Java.

## Build

I am building this with Eclipse. There is build.xml included, but it also 
depends on Eclipse.

Code has no dependencies on Eclipse, so it can be built with any Java environment.

## Usage


**Send data:**

```java
PublishPacket pkt = new PublishPacket(topic, value);
pkt.send();

```

**Listen for data:**


```java
public class MqttUdpDataSource extends SubServer {

...

    @Override
    protected void processPacket(IPacket p) throws IOException {
        if (p instanceof PublishPacket) {
            System.out.println("Pub pkt "+p);
            PublishPacket pp = (PublishPacket) p;			
            ...
        }

    }

}

```
