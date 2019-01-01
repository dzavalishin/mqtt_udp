---
---

# Hi and welcome to MQTT/UDP project

MQTT/UDP is as simple as possible protocol derived from, guess what, MQTT.
As name tells, MQTT/UDP using UDP broadcast as transport.

[GutHub repository](https://github.com/dzavalishin/mqtt_udp)

## MQTT/UDP is

### Extremely simple

There's Arduino sketch which is less than 3Kb. Though, full implementation is bigger,
but if needed, it can be extremely small.

### Extremely fast, minimap possible latency

### Excludes broker (which is single point of failure)

### Lowers network traffic (each masurement is sent exactly once to all) 

### Quite reliable 

If we use it for sensors, which usually resend data every few seconds or so, sporadic packet loss is not a real problem.

And practical checks show that UDP packet loss in nowadays network is less than 0.5% even when traffic is high/

### Can be supported even on a hardware which can not support TCP - in fact, only UDP send is required

### Zero configuration - a sensor node needs no setup, it just broadcasts its data.

### With some extension can be used on simplex channels and/or channels with native broadcast ability (radio,RS485)

