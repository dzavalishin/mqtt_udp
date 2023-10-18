.. _go-lang-api:

Go Language API Reference
-------------------------

This document is incomplete yet.


There is a native MQTT/UDP implementation in Go. You can browse sources at https://github.com/dzavalishin/mqtt_udp/tree/master/lang/go repository.


.. index:: single: send

Send data::


    import "dz.ru/mqttudp"
    mqttudp.Publish(topic, value)


.. index:: single: listen

Listen for data::

	type myServer struct {
	}

	func (s myServer) Accept(packet mqttudp.MqttPacket) {
		packet.Dump()
	}

	...

	// in main

	var s myServer
	mqttudp.SubServer(s)

MqttPacket structure methods::


**func (pkt MqttPacket) GetType() PType**
	Get type of packet

**func (pkt MqttPacket) GetReplyTo() int**
	Get id of packet this one replied to

func (pkt MqttPacket) GetId() int
	Get id of this packet

func (pkt MqttPacket) Send() error
	Send this packet

func Publish(topic string, value string) error
	Send publish packet - publist value for a topic

func Subsribe(topic string) error
	Send subscribe packet

func Ping() error
	Send ping

func PingResponce() error
	Send ping responce


