package proto

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
	"mqttUdp/misc"
	"net"
)

type MqttPacket struct {
	from_ip *net.UDPAddr ///< Sender IP address

	packetType  misc.PType ///< packet type. Upper 4 bits, not shifted.
	packetFlags byte       ///< Packet flags (QoS, etc). Lower 4 bits.

	total int ///< Length of the rest of pkt down from here.

	pkt_id int ///< Packet ID, supported by TTR ('n').

	topic []byte ///< Topic string
	value []byte ///< Value string

	is_signed bool ///< This packet has correct digital signature

	reply_to int ///< ID of packet we reply to

	// Internal working place, do not touch from outside of lib

	resend_count int
	ack_count    int
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

func NewMqttPacket(t misc.PType, topic []byte, data []byte) MqttPacket {
	var pp MqttPacket
	pp.Clear()

	pp.packetType = t
	pp.topic = topic
	pp.value = data

	return pp
}

func (pkt MqttPacket) GetType() misc.PType {
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

/**
 * Increment ack counter
**/

func (pkt MqttPacket) IncAckCount() {
	pkt.ack_count++
}

func (pkt MqttPacket) GetResendCount() int {
	return pkt.resend_count
}

func (pkt MqttPacket) IncResendCount() {
	pkt.resend_count++
}

// -----------------------------------------------------------------------
// Build
// -----------------------------------------------------------------------

//static int32_t packet_number_generator;

var packet_number_generator int

/**
 * @brief Build outgoing binary packet representation.
 *
 * @param buf      Buffer to put resulting packet to
 * @param p        Packet to encode
 * @param out_len  Resulting length of build packet in bytes
 *
 * @return length of packet
 *
**/
func (p *MqttPacket) BuildAnyPkt(buf []byte) (int, error) {
	var blen = len(buf)
	//int rc;
	// TODO check for consistency - if pkt has to have topic & value and has it

	//tlen = p.topic != nil ? len(p->topic_len) : 0;
	//dlen = p->value != nil ? len(p->value_len) : 0;
	var tlen = len(p.topic)
	var dlen = len(p.value)

	var bp = 0
	var out_len = 0

	buf[bp] = (p.packetType & 0xF0) | (p.packetFlags & 0x0F)
	bp++
	blen--

	// MQTT payload size, not incl TTRs
	var total = tlen + dlen + 2

	// Not supported in MQTT/UDP: if(MQTT_UDP_FLAGS_HAS_ID(p->pflags)) total += 2;

	var used = 0
	var err error
	used, err = pack_len(buf, total)
	if err != nil {
		return 0, err
	}

	bp += used

	if total > blen {
		return 0, misc.GlobalErrorHandler(misc.Memory, "out of memory", "")
	}

	/* Not supported in MQTT/UDP
	   if(MQTT_UDP_FLAGS_HAS_ID(p->pflags))
	   {
	       *bp++ = (p->pkt_id >> 8) & 0xFF;
	       *bp++ = p->pkt_id & 0xFF;
	       blen -= 2;
	   }
	*/

	if tlen > 0 {
		// Encode topic len
		buf[bp] = byte((tlen >> 8) & 0xFF)
		bp++
		buf[bp] = byte(tlen & 0xFF)
		bp++
		blen -= 2

		//var topic * byte = p.topic
		var topic = 0
		//NB! Must be UTF-8
		for tlen > 0 {
			tlen--
			if blen == 0 {
				return 0, misc.GlobalErrorHandler(misc.Memory, "out of memory", "")
			}
			buf[bp] = p.topic[topic]
			bp++
			topic++
			blen--
		}

		//const char *data = p.value
		var data = 0
		for dlen > 0 {
			dlen--

			if blen == 0 {
				return 0, misc.GlobalErrorHandler(misc.Memory, "out of memory", "")
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

	var rc = encode_int32_TTR(&bp, &blen, 'n', p.pkt_id)
	if rc != nil {
		return 0, rc
	}

	/* TODO
	    // NB! This is a signature TTR, it must me the last one.

	    // Signature TTR needs this many bytes
	//#define SIGNATURE_TTR_SIZE (MD5_DIGEST_SIZE+2)
	    if(mqtt_udp_hmac_md5 != 0)
	    {
	        // Will sign
	        if( blen < SIGNATURE_TTR_SIZE )
	            return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "signature" );

	        //unsigned char signature[MD5_DIGEST_SIZE];
	        unsigned char *signature = (unsigned char *)bp+2;
	        mqtt_udp_hmac_md5( (unsigned char *)buf, bp-buf, signature );
	        bp[0] = 's';
	        bp[1] = ( 0x7F & MD5_DIGEST_SIZE );
	        //bp[1] = MD5_DIGEST_SIZE;

	        bp += SIGNATURE_TTR_SIZE;
	        blen -= SIGNATURE_TTR_SIZE;
	    }
	*/

	return out_len, nil
}

/*
// -----------------------------------------------------------------------
// TTRs
// -----------------------------------------------------------------------

func encode_TTR( buf [] byte, bp * int, ttype byte, data [] byte  ) {
	dlen = len(data);

    //if( *blen < 2 )         return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );

    // TTR type byte
    *(*bp)++ = type;

    // TTR content len
    used = 0;
    int rc = pack_len( *bp, blen, &used, dlen );
    if( rc ) return rc;
    *bp += used;

    if( *blen < dlen )
        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "" );

    //TTR content
    memcpy( *bp, data, dlen );
    *bp += dlen;

    return 0;
}

int encode_int32_TTR( char **bp, size_t *blen, char type, uint32_t value )
{
    const int bytes = 4; // 32 bits
    char out[bytes];
    int i;

    for( i = 0; i < bytes; i++ )
		out[i] = (char)(value >> (8*(bytes - i - 1)) );

    return encode_TTR( bp, blen, type, out, sizeof out );
}

func encode_int64_TTR( buf [] byte, bp * int, size_t *blen, type byte, uint64_t value ) int, error
{
    const int bytes = 8; // 64 bits
    char out[bytes];
    int i;

    for( i = 0; i < bytes; i++ )
		out[i] = (char)(value >> (8*(bytes - i - 1)) );

    return encode_TTR( bp, blen, type, out, sizeof out );
}



*/

// -----------------------------------------------------------------------
// Bits
// -----------------------------------------------------------------------

// / Encode payload length. Return bytes used
func pack_len(buf []byte, data_len int) (int, error) {
	var used int = 0
	var blen int = len(buf)

	for {
		if blen == 0 {
			return 0, misc.GlobalErrorHandler(misc.Memory, "out of memory", "")
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
