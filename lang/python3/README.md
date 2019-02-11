# MQTT/UDP Python (3.x) implementation

You can check other languages implementations also - <https://github.com/dzavalishin/mqtt_udp>

## Dirs

  mqttudp					- MQTT/UDP library in Python language

  examples					- Library usage examples

  gate.sh					- Shell script to start bidirectional MQTT/UDP to MQTT broker gateway
  mqttudpgate.service		- Unix systemctl service definition for gateway

## Examples

  pub.py                	- Send one message

  dump.py					- Print all MQTT/UDP traffic
  listen.py					- Print packets only if content is changeg

  mqtt_udp_to_openhab.py 	- Translate all the data to OpenHAB REST API
  openhab_to_udp.py			- Translate all the data from OpenHAB REST API (default sitemap) to MQTT/UDP

  seq_storm_send.py 		- Send seqentially numbered packets as fast as possible
  seq_storm_check.py 		- Check packets for sequentialness and calc speed

  mqtt_bidir_gate.py		- Translate data between MQTT/UDP and traditional MQTT
  mqtt_broker_to_udp.py		- One way, to MQTT/UDP
  mqtt_udp_to_broker.py		- One way, from MQTT/UDP

  random_to_udp.py			- Generate traffic with random numbers

  ping.py					- Send ping and print replies. Not all the implementations respond to ping yet.

## Possible requirements

  Some code examples may require paho-mqtt and websocket-client

## Usage

**Send data:**

```python
import mqttudp.engine as me

if __name__ == "__main__":
    me.send_publish( "test_topic", "Hello, world!" )
```

**Listen for data:**

```python
import mqttudp.engine as me

def recv_packet(pkt):
    if pkt.ptype != me.PacketType.Publish:
        print( str(pkt.ptype) + ", " + pkt.topic + "\t\t" + str(addr) )
        return
    print( pkt.topic+"="+pkt.value+ "\t\t" + str(pkt.addr) )

if __name__ == "__main__":
    me.listen(recv_packet)
```
