# MQTT/UDP
Simpified version of MQTT over UDP: Network is broker!

[See Russian version / Русская версия здесь](./README.ru.md)

MQTT is a cute simple protocol well suited for IoT and similar things.

But it can be even simpler and still be very usable: MQTT/UDP is
merely an MQTT Publish packets broadcast over an UDP.

## MQTT/UDP is 

* Extremely simple
* Extremely fast, minimap possible latency
* Excludes broker (which is single point of failure)
* Lowers network traffic (each masurement is sent exactly once to all) 
* Reasonably reliable (if we use it for sensors, which usually resend data every few seconds or so)
* Can be supported even on a hardware which can not support TCP - in fact, only UDP send is required
* Zero configuration - a sensor node needs no setup, it just broadcasts its data.
* With some extension can be used on simplex channels and/or channels with native broadcast ability (radio,RS485)

## For further reading

* [MQTT/UDP Wiki](../../wiki)
* [MQTT/UDP Topologies](./dox/Topologies.md)

## This repository contains

* A simple MQTT/UDP implementation in some popular programming languages.
* A simplest MQTT to MQTT/UDP bridge implementation in Pyton.
* A tool to send data from MQTT/UDP to OpenHAB 
* A [debug application](https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help) to dump MQTT/UDP traffic ([tools/viewer](tools/viewer)).
* Other tools and utilities

## If you want to help a project

Feel free to:

* Add implementation in your favorite programming language
* Write a bridge to classic MQTT protocol (we have very simple one here written in Python)
* Extend your favorite MQTT broker or IoT system (OpenHAB?) with MQTT/UDP support
* Check MQTT/UDP specification/implementation against MQTT spec. We must be compatible where possible.

It is really easy.

## Reasons to avoid MQTT/UDP

* You need to transfer long payloads. On some systems size of UDP datagram is limited.
* You need to know if datagram was delivered for sure. It is impossible with UDP.

## Ways to extend MQTT/UDP

* It seems to be reasonable to add some kind of signature to packets to prevent data spoofing. 
* Broadcast is not the best way to transmit data. Multicast is much better. Though multicast is not so well supported in IoT OS's or monitors.

## Fast start instructions

* Clone repository to local disk
* Read [HOWTO](https://raw.githubusercontent.com/dzavalishin/mqtt_udp/master/HOWTO) file

## Support tools

This repository contains tools to support MQTT/UDP integration and test:

* WireShark dissector to see protocol data on the network in [lua](https://github.com/dzavalishin/mqtt_udp/tree/master/lua) directory
* GUI tool to show current state of data transmitted in [tools/viewer](https://github.com/dzavalishin/mqtt_udp/tree/master/tools/viewer) directory; see also [viewer help page](https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help).
* Random data generator (random_to_udp.py) in [python3/examples](https://github.com/dzavalishin/mqtt_udp/tree/master/python3/examples) directory
* Send/check for sequentially numbered packets. See seq_storm_send.py and seq_storm_check.py in [python3/examples](https://github.com/dzavalishin/mqtt_udp/tree/master/python3/examples) directory


## Code examples

### Python

**Send data:**

```python
import mqttudp.engine

if __name__ == "__main__":
    mqttudp.engine.send_publish_packet( "test_topic", "Hello, world!" )
```

**Listen for data:**

```python
import mqttudp.engine

def recv_packet(ptype,topic,value,pflags,addr):
    if ptype != "publish":
        print( ptype + ", " + topic + "\t\t" + str(addr) )
        return
    print( topic+"="+value+ "\t\t" + str(addr) )

if __name__ == "__main__":
    mqttudp.engine.listen(recv_packet)
```


### Java

**Send data:**

```java
PublishPacket pkt = new PublishPacket(topic, value);
pkt.send();

```

**Listen for data:**


```java
PacketSourceServer ss = new PacketSourceServer();
ss.setSink( pkt -> { System.out.println("Got packet: "+pkt);});

```


### C

**Send data:**

```c

int fd = mqtt_udp_socket();
int rc = mqtt_udp_send_publish( fd, topic, value );

```

**Listen for data:**

```c

int main(int argc, char *argv[])
{
    ...

    int rc = mqtt_udp_recv_loop( mqtt_udp_dump_any_pkt );

    ...
}

int mqtt_udp_dump_any_pkt( struct mqtt_udp_pkt *o )
{

    printf( "pkt %x flags %x, id %d",
            o->ptype, o->pflags, o->pkt_id
          );

    if( o->topic_len > 0 )
        printf(" topic '%s'", o->topic );

    if( o->value_len > 0 )
        printf(" = '%s'", o->value );

    printf( "\n");
}


```



