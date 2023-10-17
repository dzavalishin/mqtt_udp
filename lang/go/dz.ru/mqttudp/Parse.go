package mqttudp

import (
	"net"
	"reflect"
)

/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2023 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Generalized MQTT/UDP packet parser
 *
**/

// / Sanity check size
//const MAX_SZ = 4096 // TODO move me

// -----------------------------------------------------------------------
// parse
// -----------------------------------------------------------------------

//#define MQTT_UDP_PKT_HAS_ID(pkt)  ((pkt.pflags) & 0x6)

//type PacketProcessor interface {	Process(pkt MqttPacket) error }

/*
Parse incoming packet.

Call callback function with resulting packet.

	@param raw       Incoming binary packet data from UDP packet.
	@param from_ip   Source IP address of packet.
	@param acceptor  User function to call on packet parsed.
*/
func ParseBinary(raw []byte, from_ip *net.Addr) (MqttPacket, error) {
	var plen = len(raw)
	var err error = nil

	var o MqttPacket
	var pkt = 0 // packet parse position

	o.Clear()

	if plen < 2 {
		return o, GlobalErrorHandler(Proto, "packet len < 2", "")
	}

	o.from_ip = from_ip
	o.packetType = PType(raw[0])
	pkt++
	o.packetFlags = byte(o.packetType) & 0xF
	o.packetType &= 0xF0
	o.total = decode_size(raw, &pkt)
	o.topic = nil
	o.value = nil

	if o.total+2 > plen {
		return o, GlobalErrorHandler(Proto, "packet too short", "")
	}

	var ttrs_start int = pkt + o.total // end of payload, start of TTRs

	var tlen = 0
	var vlen = 0

	// Packets with topic?
	switch o.packetType {
	case SUBSCRIBE:
		break

	case PUBLISH:
		break

	default:
		goto parse_ttrs
	}

	tlen = decode_topic_len(raw[pkt:])
	pkt += 2

	if tlen > MAX_SZ {
		return o, GlobalErrorHandler(Proto, "packet too long", "")
	}

	if pkt+tlen > o.total+2 {
		return o, GlobalErrorHandler(Proto, "packet topic len > pkt len", "")
	}

	o.topic = raw[pkt : pkt+tlen]

	pkt += tlen

	vlen = o.total - pkt + 2
	if vlen > MAX_SZ {
		return o, GlobalErrorHandler(Proto, "packet value len > pkt len", "")
	}

	// Packet with value?
	if o.packetType != PUBLISH {
		goto parse_ttrs
	}

	o.value = raw[pkt : pkt+vlen]

parse_ttrs:

	var ttrs = raw[ttrs_start:] // Current position in TTRs
	//var ttrs_len = plen - ttrs_start

	//fmt.Printf("TTRs  len=%d, plen=%d\n", len(ttrs), plen)

	for len(ttrs) > 0 {
		var ttr_type = ttrs[0]
		var ttr_pos = 1 // skip type

		var ttr_len = decode_size(ttrs, &ttr_pos)

		if ttr_len <= 0 {
			return o, GlobalErrorHandler(Proto, "TTR len <= 0", "")
		}

		//fmt.Printf("TTR type = %c 0x%X len=%d\n", ttr_type, ttr_type, ttr_len)
		// Have TTR, process it
		switch ttr_type {
		case 'n':
			o.pkt_id = ttr_decode_int32(ttrs[ttr_pos:])
			break
		case 'r':
			o.reply_to = ttr_decode_int32(ttrs[ttr_pos:])
			break
		case 's':
			if ttr_len < 16 {
				err = GlobalErrorHandler(Proto, "signature TTR len < 16", "")
				if err != nil {
					return o, err
				}
			}
			o.is_signed = false // TODO TTR_check_signature(pstart, ttr_start-pstart, ttrs)
			//o.is_signed = TTR_check_signature(raw, ttrs_start, ttrs[ttr_pos:])
			break
		default:
			break
		}

		ttr_pos += ttr_len

		//ttrs_len -= ttr_pos // type & len fields
		//ttrs_len -= ttr_len // TTR data

		//ttrs = ttrs[ttr_len+ttr_pos:]

		if len(ttrs) < ttr_pos+2 {
			//return o, GlobalErrorHandler(Proto, "TTR len < 2", "")
			// Ignore junk at end of pkt
			return o, nil
		}

		ttrs = ttrs[ttr_pos:]

		/*if ttrs_len < 0 {
			return o, GlobalErrorHandler(Proto, "TTRs len < 0", "")
		}*/
	}

	return o, err
}

/*
Parse incoming packet.

Call callback function with resulting packet.

	@param raw       Incoming binary packet data from UDP packet.
	@param from_ip   Source IP address of packet.
	@param acceptor  User function to call on packet parsed.
*/
func ParseAndProcess(raw []byte, from_ip *net.Addr, acceptor MqttUdpInput) error {
	o, err := ParseBinary(raw, from_ip)

	if err != nil {
		return err
	}

	o.call_packet_listeners()

	if acceptor != nil {
		acceptor.Accept(o)
	}

	return nil
}

// -----------------------------------------------------------------------
//
// decoders
//
// -----------------------------------------------------------------------

// Decode payload size dynamic length int
func decode_size(pkt []byte, pos *int) int {
	var ret int = 0

	for {
		var b byte = pkt[*pos]
		(*pos)++
		ret |= int(b & ^byte(0x80))

		if (b & 0x80) == 0 {
			return ret
		}

		ret <<= 7
	}
}

// Decode fixed 2-byte integer.
func decode_topic_len(pkt []byte) int {
	return int(pkt[0])<<8 | int(pkt[1])
}

func ttr_decode_int32(data []byte) int {
	var v int = 0

	v = int(data[0]) << 24
	v |= int(data[1]) << 16
	v |= int(data[2]) << 8
	v |= int(data[3])

	return v
}

func ttr_check_signature(buf []byte, pkt_len int, in_signature []byte) bool {

	//var us_signature [MD5_DIGEST_SIZE]byte
	us_signature := hmac_md5(buf[0:pkt_len]) // md5.Sum(buf[0:pkt_len]) // mqtt_udp_hmac_md5(pkt_start, pkt_len, us_signature)

	//var ok = !memcmp(in_signature, us_signature, MD5_DIGEST_SIZE)
	var ok = reflect.DeepEqual(us_signature, in_signature)
	if !ok {
		GlobalErrorHandler(Proto, "Incorrect signature", "")
	}

	return true // TODO must return ok?
}
