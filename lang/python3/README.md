# MQTT/UDP Python (3.x) implementation.

You can check other languages implementations also - https://github.com/dzavalishin/mqtt_udp

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

  seq_storm_send.py 		- Send seqentially numbered packets as fast as possible
  seq_storm_check.py 		- Check packets for sequentialness and calc speed

  bidirectional_gate.py		- Translate data between MQTT/UDP and traditional MQTT

  random_to_udp.py			- Generate traffic with random numbers

  ping.py					- Send ping and print replies. Not all the implementations respond to ping yet.

## Usage

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
