package mqttudp

/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2023 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * MQTT/UDP packet class methods
 *
**/

import (
	"fmt"
	"net"
)

type MqttPacket struct {
	from_ip *net.Addr ///< Sender IP address

	packetType  PType // packet type. Upper 4 bits, not shifted.
	packetFlags byte  // Packet flags (QoS, etc). Lower 4 bits.

	total int // Length of the rest of pkt down from here.

	pkt_id int // Packet ID, supported by TTR ('n').

	topic []byte // Topic string
	value []byte // Value string

	is_signed bool // This packet has correct digital signature

	reply_to int // ID of packet we reply to

	// Internal working place, do not touch from outside of lib

	resend_count int
	ack_count    int
}

type MqttUdpInput interface {
	Accept(packet MqttPacket)
}

func (p *MqttPacket) Clear() {
	p.from_ip = nil
	p.packetType = 0
	p.packetFlags = 0
	p.total = 0
	p.pkt_id = 0
	p.topic = nil
	p.value = nil
	p.is_signed = false
	p.reply_to = 0
	p.resend_count = 0
	p.ack_count = 0
}

func (pkt *MqttPacket) Get_QOS() int {
	return int(((pkt.packetFlags) >> 1) & 0x3)
}

func (pkt *MqttPacket) Set_QOS(qos int) {
	pkt.packetFlags &= 0x6
	pkt.packetFlags |= byte(((qos) & 0x3) << 1)
}

func NewMqttPacket(t PType, topic []byte, data []byte) MqttPacket {
	var pp MqttPacket
	pp.Clear()

	pp.packetType = t
	pp.topic = topic
	pp.value = data

	return pp
}

func (pkt MqttPacket) GetType() PType {
	return pkt.packetType
}

func (pkt MqttPacket) GetReplyTo() int {
	return pkt.reply_to
}

func (pkt MqttPacket) GetId() int {
	return pkt.pkt_id
}

func (pkt MqttPacket) GetAckCount() int {
	return pkt.ack_count
}

// Increment ack counter
func (pkt MqttPacket) IncAckCount() {
	pkt.ack_count++
}

func (pkt MqttPacket) GetResendCount() int {
	return pkt.resend_count
}

// Increment resend counter
func (pkt MqttPacket) IncResendCount() {
	pkt.resend_count++
}

func (pkt MqttPacket) Send() error {
	return build_and_send(pkt)
}

/*
Compose and send PUBLISH packet.

@param topic  Message topic
@param data   Message value, usually text string
*/
func Publish(topic string, value string) error {
	p := NewMqttPacket(PUBLISH, []byte(topic), []byte(value))
	return p.Send()
}

/*
Compose and send SUBSCRIBE packet.

@param topic  Message topic

@returns 0 if ok, or error code
*/
func Subsribe(topic string) error {
	p := NewMqttPacket(PUBLISH, []byte(topic), nil)
	return p.Send()
}

// Send empty packet of any type
func Empty(ptype PType) error {
	p := NewMqttPacket(ptype, nil, nil)
	return p.Send()
}

func Ping() error { return Empty(PINGREQ) }

func PingResponce() error { return Empty(PINGRESP) }

// -----------------------------------------------------------------------
//
// dump
//
// -----------------------------------------------------------------------

var ptname = []string{"?0x00",
	"CONNECT", "CONNACK", "PUBLISH", "PUBACK",
	"PUBREC", "PUBREL", "PUBCOMP", "SUBSCRIBE",
	"SUBACK", "UNSUBSCRIBE", "UNSUBACK", "PINGREQ",
	"PINGRESP", "DISCONNECT",
	"?0xF0"}

// Dump packet.
func (o *MqttPacket) Dump() {
	var tn = ptname[o.packetType>>4]

	var from = "(src unknown)"

	if o.from_ip != nil {
		from = (*o.from_ip).String()
	}

	fmt.Printf("pkt %10s flags %x, id %8x from %s",
		tn, o.packetFlags, o.pkt_id, from)
	/*int(0xFF&(o.from_ip>>24)),	int(0xFF&(o.from_ip>>16)),	int(0xFF&(o.from_ip>>8)),	int(0xFF&(o.from_ip))) */

	if len(o.topic) > 0 {
		fmt.Printf(" topic '%s'", o.topic)
	}

	if len(o.value) > 0 {
		fmt.Printf(" = '%s'", o.value)
	}

	if o.is_signed {
		fmt.Printf(" SIGNED")
	}

	fmt.Printf("\n")
}
