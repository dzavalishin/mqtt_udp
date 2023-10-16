package proto

import (
	"log"
	"mqttUdp/misc"
	"net"
)

/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp

 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Generalized MQTT/UDP packet parser
 *
**/

const MAX_SZ = 4096 // TODO move me

// -----------------------------------------------------------------------
// parse
// -----------------------------------------------------------------------

//#define MQTT_UDP_PKT_HAS_ID(pkt)  ((pkt.pflags) & 0x6)

/// Sanity check size
//#define MAX_SZ (4*1024)
/// How many bytes are processed already
//#define CHEWED (pkt - pstart)

type PacketProcessor interface {
	Process(pkt MqttPacket) error
}

/**
 *
 * @brief Parse incoming packet.
 *
 * Call callback function with resulting packet.
 *
 * @param pkt       Incoming binary packet data from UDP packet.
 * @param plen      Incoming packet data length.
 * @param from_ip   Source IP address of packet.
 * @param callback  User function to call on packet parsed.
 *
 * @return 0 on success or error code.
**/
func Parse_any_pkt(raw []byte, from_ip net.Addr, acceptor MqttUdpInput) error {
	var plen = len(raw)
	var err error = nil

	var o MqttPacket
	//var pstart * byte = &raw
	//var pkt * byte = &raw
	var pkt = 0

	//if( plen <= 2 )
	if plen < 2 {
		return misc.GlobalErrorHandler(misc.Proto, "packet len < 2", "")
	}

	//mqtt_udp_clear_pkt( &o );
	o.Clear()

	o.from_ip = &from_ip
	o.packetType = misc.PType(raw[0])
	pkt++
	o.packetFlags = byte(o.packetType) & 0xF
	o.packetType &= 0xF0
	o.total = mqtt_udp_decode_size(raw, &pkt)
	o.topic = nil
	o.value = nil

	if o.total+2 > plen {
		return misc.GlobalErrorHandler(misc.Proto, "packet too short", "")
	}
	//const char *end_hdr = pkt; // end of header, start of payload
	var ttrs_start int = pkt + o.total // end of payload, start of TTRs

	// NB! MQTT/UDP does not use variable header (ID) field
	/*
	   if(MQTT_UDP_FLAGS_HAS_ID(o.pflags))
	   {
	       o.pkt_id = (pkt[0] << 8) | pkt[1];
	       pkt += 2;
	       o.total -= 2;
	   }
	   else
	       o.pkt_id = 0;
	*/

	var tlen = 0
	var vlen = 0

	// Packets with topic?
	switch o.packetType {
	case misc.SUBSCRIBE | misc.PUBLISH:
		break

	default:
		goto parse_ttrs
	}

	tlen = mqtt_udp_decode_topic_len(raw[pkt:])
	pkt += 2

	if tlen > MAX_SZ {
		return misc.GlobalErrorHandler(misc.Proto, "packet too long", "")
	}

	if pkt+tlen > o.total+2 {
		return misc.GlobalErrorHandler(misc.Proto, "packet topic len > pkt len", "")
	}

	o.topic = make([]byte, tlen+2)
	if o.topic == nil {
		return misc.GlobalErrorHandler(misc.Memory, "out of memory", "")
	}
	//strlcpy(o.topic, pkt, tlen+1)
	o.topic = raw[pkt : pkt+tlen]
	//o.topic_len = strnlen( o.topic, MAX_SZ );
	//o.topic_len = strnlen(o.topic, tlen)

	pkt += tlen

	vlen = o.total - pkt + 2
	if vlen > MAX_SZ {
		return misc.GlobalErrorHandler(misc.Proto, "packet value len > pkt len", "")
	}

	// Packet with value?
	if o.packetType != misc.PUBLISH {
		goto parse_ttrs
	}

	//vlen++; // strlcpy needs place for zero
	o.value = make([]byte, vlen+2)
	if o.value == nil {
		return misc.GlobalErrorHandler(misc.Memory, "out of memory", "")
	}
	//strlcpy(o.value, pkt, vlen+1)
	o.value = raw[pkt : pkt+vlen]
	//o.value_len = strnlen( o.value, MAX_SZ );
	//o.value_len = strnlen(o.value, vlen)

parse_ttrs:
	;
	//#if 1
	var ttrs = raw[ttrs_start:] // Current position in TTRs
	var ttrs_len = plen - ttrs_start

	//printf("TTRs  len=%d, plen=%d\n", ttrs_len, plen );

	for ttrs_len > 0 {
		//var ttr_start = ttrs
		var ttr_type = ttrs[0]
		var ttr_pos = 1

		var ttr_len = mqtt_udp_decode_size(ttrs, &ttr_pos)

		if ttr_len <= 0 {
			err = misc.GlobalErrorHandler(misc.Proto, "TTR len < 0", "")
			goto cleanup
		}

		//printf("TTR type = %c 0x%X len=%d\n", ttr_type, ttr_type, ttr_len );
		// Have TTR, process it
		switch ttr_type {
		case 'n':
			o.pkt_id = ttr_decode_int32(ttrs[ttr_pos:])
			break
		case 'r':
			o.reply_to = ttr_decode_int32(ttrs[ttr_pos:])
			break
		case 's':
			if ttr_len <= 0 {
				err = misc.GlobalErrorHandler(misc.Proto, "signature TTR len != 16", "")
				if err != nil {
					goto cleanup
				}
			}
			o.is_signed = false // TODO TTR_check_signature(pstart, ttr_start-pstart, ttrs)
			break
		default:
			break
		}

		ttrs_len -= ttr_pos // type & len fields
		ttrs_len -= ttr_len // TTR data

		ttrs = ttrs[ttr_len+ttr_pos:]

		if ttrs_len < 0 {
			err = misc.GlobalErrorHandler(misc.Proto, "TTRs len < 0", "")
			goto cleanup
		}
	}
	//#endif

	recv_reply(&o)
	o.call_packet_listeners()
	//callback( &o );
	acceptor.Accept(o)

cleanup:
	/*
	  //if( o.topic ) free( o.topic );
	  //if( o.value ) free( o.value );
	  mqtt_udp_free_pkt( &o ); */

	return err
}

// -----------------------------------------------------------------------
//
// decoders
//
// -----------------------------------------------------------------------

// / Decode payload size dynamic length int
func mqtt_udp_decode_size(pkt []byte, pos *int) int {
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

// / Decode fixed 2-byte integer.
func mqtt_udp_decode_topic_len(pkt []byte) int {
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

/*
func ttr_check_signature(pkt_start *byte, pkt_len int, in_signature *byte) bool {
	// Not set up, can't check
	if mqtt_udp_hmac_md5 == 0 {
		return false
	}

	var us_signature [MD5_DIGEST_SIZE]byte
	mqtt_udp_hmac_md5(pkt_start, pkt_len, us_signature)

	var ok = !memcmp(in_signature, us_signature, MD5_DIGEST_SIZE)
	if !ok {
		GlobalErrorHandler(misc.Proto, "Incorrect signature", "")
	}
	return true
} */

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

/**
 * @brief Dump packet.
 *
 * @param o Packet pointer.
 *
 * @return Allways 0.
**/
func (o *MqttPacket) Dump() {
	var tn = ptname[o.packetType>>4]

	log.Printf("pkt %10s flags %x, id %8x from %s",
		tn, o.packetFlags, o.pkt_id, (*o.from_ip).String())
	/*int(0xFF&(o.from_ip>>24)),
	int(0xFF&(o.from_ip>>16)),
	int(0xFF&(o.from_ip>>8)),
	int(0xFF&(o.from_ip))) */

	if len(o.topic) > 0 {
		log.Printf(" topic '%s'", o.topic)
	}

	if len(o.value) > 0 {
		log.Printf(" = '%s'", o.value)
	}

	if o.is_signed {
		log.Printf(" SIGNED")
	}

	log.Printf("\n")
}

/**
 *
 * @brief Default packet processing, called from mqtt_udp_parse_any_pkt()
 *
 * * Reply to ping
 *
 * @todo Reply to SUBSCRIBE? Not sure.
 * @todo Reply with PUBACK for PUBLISH with QoS
 * @todo Error handling
 *
**/

func recv_reply(pkt *MqttPacket) {
	//int fd = mqtt_udp_get_send_fd();

	switch pkt.packetType {
	case misc.PINGREQ:
		// TODO err check
		//if( fd > 0 ) mqtt_udp_send_ping_responce( fd, pkt->from_ip );
		// TODO mqtt_udp_send_ping_responce()
		break
	//case PTYPE_SUBSCRIBE:
	//case PTYPE_PUBLISH:
	default:
		break
	}
}
