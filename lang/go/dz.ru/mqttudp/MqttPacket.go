package mqttudp

/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 *
 * @file
 * @brief Generalized MQTT/UDP packet builder
 *
**/

import (
	"crypto/md5"
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

// -----------------------------------------------------------------------
// Build
// -----------------------------------------------------------------------

var packet_number_generator int

/*
Build outgoing binary packet representation.

@param buf      Buffer to put resulting packet to
@param p        Packet to encode
@param out_len  Resulting length of build packet in bytes

@return length of packet
*/
func (p MqttPacket) BuildAnyPkt(buf []byte) (int, error) {
	var blen = len(buf)
	//int rc;
	// TODO check for consistency - if pkt has to have topic & value and has it

	//tlen = p.topic != nil ? len(p->topic_len) : 0;
	//dlen = p->value != nil ? len(p->value_len) : 0;
	var tlen = len(p.topic)
	var dlen = len(p.value)

	var bp = 0

	buf[bp] = byte(int(p.packetType)&0xF0) | (p.packetFlags & 0x0F)
	bp++
	blen--

	// MQTT payload size, not incl TTRs
	var total = tlen + dlen + 2

	var used = 0
	var err error
	used, err = pack_len(buf, total)
	if err != nil {
		return 0, err
	}

	bp += used

	if total > blen {
		return 0, GlobalErrorHandler(Memory, "out of memory", "")
	}

	if tlen > 0 {
		// Encode topic len
		buf[bp] = byte((tlen >> 8) & 0xFF)
		bp++
		buf[bp] = byte(tlen & 0xFF)
		bp++
		blen -= 2

		var topic = 0
		//NB! Must be UTF-8
		for tlen > 0 {
			tlen--
			if blen == 0 {
				return 0, GlobalErrorHandler(Memory, "out of memory", "")
			}
			buf[bp] = p.topic[topic]
			bp++
			topic++
			blen--
		}

		var data = 0
		for dlen > 0 {
			dlen--

			if blen == 0 {
				return 0, GlobalErrorHandler(Memory, "out of memory", "")
			}
			buf[bp] = p.value[data]
			bp++
			data++
			blen--
		}

	}

	if p.pkt_id == 0 {
		p.pkt_id = packet_number_generator
		packet_number_generator++
	}

	var rc = encode_int32_TTR(buf, &bp, &blen, 'n', uint32(p.GetId()))
	if rc != nil {
		return 0, rc
	}

	// NB! This is a signature TTR, it must me the last one.

	// Will sign
	if blen < SIGNATURE_TTR_SIZE {
		return 0, GlobalErrorHandler(Memory, "out of memory", "signature")
	}

	hash := md5.Sum(buf[0:bp])

	buf[bp+0] = 's'
	buf[bp+1] = (0x7F & MD5_DIGEST_SIZE)
	copy(buf[bp+2:bp+2+MD5_DIGEST_SIZE], hash[:])

	bp += SIGNATURE_TTR_SIZE
	blen -= SIGNATURE_TTR_SIZE

	return bp, nil
}

// -----------------------------------------------------------------------
// TTRs
// -----------------------------------------------------------------------

func encode_TTR(buf []byte, bp *int, blen *int, ttype byte, data []byte) error {
	var dlen = len(data)

	if *blen < 2 {
		return GlobalErrorHandler(Memory, "out of memory", "")
	}

	// TTR type byte
	buf[*bp] = ttype
	(*bp)++

	// TTR content len
	//var used = 0
	var used, rc = pack_len(buf[*bp:], dlen)
	if rc != nil {
		return rc
	}

	*bp += used

	if *blen < dlen {
		return GlobalErrorHandler(Memory, "out of memory", "")
	}

	//TTR content
	//memcpy(*bp, data, dlen)
	copy(buf[*bp:], data)
	*bp += dlen

	return nil
}

func encode_int32_TTR(buf []byte, bp *int, blen *int, ttype byte, value uint32) error {
	const bytes = 4 // 32 bits
	var out []byte = make([]byte, bytes)

	var i int
	for i = 0; i < bytes; i++ {
		out[i] = byte(value >> (8 * (bytes - i - 1)))
	}

	return encode_TTR(buf, bp, blen, ttype, out)
}

func encode_int64_TTR(buf []byte, bp *int, blen *int, ttype byte, value uint64) error {
	const bytes = 8 // 64 bits
	var out []byte = make([]byte, bytes)

	var i int
	for i = 0; i < bytes; i++ {
		out[i] = byte(value >> (8 * (bytes - i - 1)))
	}

	return encode_TTR(buf, bp, blen, ttype, out)
}

// -----------------------------------------------------------------------
// Bits
// -----------------------------------------------------------------------

// / Encode payload length. Return bytes used
func pack_len(buf []byte, data_len int) (int, error) {
	var used int = 0
	var blen int = len(buf)

	for {
		if blen == 0 {
			return 0, GlobalErrorHandler(Memory, "out of memory", "")
		}
		//if( *blen == 0 ) return 0,"out of buffer space";

		var b byte = byte(data_len % 128)
		data_len /= 128

		if data_len > 0 {
			b |= 0x80
		}

		buf[used] = b
		blen--
		used++

		if data_len == 0 {
			return used, nil
		}
	}
}
