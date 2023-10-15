package mqnet

import (
	"log"
	"mqttUdp/misc"
	"mqttUdp/proto"
	"sync"
)

/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp

 * Copyright (C) 2017-2019 Dmitry Zavalishin, dz@dz.ru
 *
 * @file
 * @brief Reliable communications layer
 *
**/

var relcom_mutex sync.Mutex

//#define MQTT_UDP_FLAGS_GET_QOS(pflags)  (((pflags) >> 1) & 0x3) ///< Get QoS field from flags
//#define MQTT_UDP_FLAGS_SET_QOS(pflags, qos) ( (pflags) &= 0x6, (pflags) |= (((qos) & 0x3) << 1) ) ///< Set QoS field of flags

// -----------------------------------------------------------------------
//
// Interface
//
// -----------------------------------------------------------------------

/**
 *
 * @brief Compose and send PUBLISH packet.
 *
 * @param topic  Message topic
 * @param data   Message value, usually text string
 *
 * @returns 0 if ok, or error code
**/
func send_publish_qos(topic []byte, data []byte, qos int) error {

	var pp proto.MqttPacket = proto.NewMqttPacket(misc.PUBLISH, topic, data)

	//var pp proto.MqttPacket = new(proto.MqttPacket)
	//int rc;

	//if( 0 == pp )        return mqtt_udp_global_error_handler( MQ_Err_Memory, -12, "out of memory", "send_publish_qos" );

	/*
		//mqtt_udp_clear_pkt( pp );
		pp.Clear()

		pp.ptype = misc.PUBLISH
		pp.topic = topic
		pp.value = data
		pp.topic_len = len(topic)
		pp.value_len = len(data)
	*/

	pp.Set_QOS(qos)

	var rc = insert_pkt(pp)
	if rc != nil {
		// Still attempt to send!
		build_and_send(pp)

		return rc
	}

	return build_and_send(pp)
}

// -----------------------------------------------------------------------
//
// Listener
//
// -----------------------------------------------------------------------

func (p relcom_packet_processor) Process(pkt proto.MqttPacket) error {
	if pkt.GetType() == misc.PUBACK {
		log.Printf("got ack to %d\n", pkt.GetReplyTo())
		if pkt.GetReplyTo() == 0 {
			misc.GlobalErrorHandler(misc.Proto, "puback reply_to 0", "relcom_listener")
			return nil
		}
		delete_pkt(pkt.GetReplyTo(), pkt.Get_QOS())
	}

	return nil
}

type relcom_packet_processor struct {
}

var rpp relcom_packet_processor

// -----------------------------------------------------------------------
//
// Init
//
// -----------------------------------------------------------------------

/**
 *
 * Must be called to init us
 *
**/

func RelcomInit() {
	//ARCH_MUTEX_INIT(relcom_mutex)
	proto.AddPacketListener(rpp)
}

/**
 *
 * @brief Must be called by application once in 100 msec.
 *
 * Does housekeeping: packets resend, cleanup.
 *
**/
func relcom_housekeeping() {
	resend_pkts()
}

// -----------------------------------------------------------------------
//
// Send
//
// -----------------------------------------------------------------------

func build_and_send(pp proto.MqttPacket) error {
	var buf []byte = make([]byte, misc.PKT_BUF_SIZE)
	var out_size int
	//int rc;
	//mqtt_udp_dump_any_pkt( &p );
	var rc error
	out_size, rc = pp.BuildAnyPkt(buf)
	if rc != nil {
		return rc
	}

	//mqtt_udp_dump( buf, out_size );

	return send_pkt(getSendSocket(), buf, out_size)
}

// -----------------------------------------------------------------------
//
// Outgoing list
//
// -----------------------------------------------------------------------

// Naive fixed size array impl
var MAX_OUTGOING_PKT = 30

var MIN_LOW_QOS_ACK = 2

var MAX_RESEND_COUNT = 3

var outgoing []*proto.MqttPacket = make([]*proto.MqttPacket, MAX_OUTGOING_PKT)

func insert_pkt(pp proto.MqttPacket) error {
	relcom_mutex.Lock()
	defer relcom_mutex.Unlock()

	var i int
	for i = 0; i < MAX_OUTGOING_PKT; i++ {
		if outgoing[i] == nil {
			outgoing[i] = &pp

			return nil
		}
	}

	relcom_mutex.Unlock()

	return misc.GlobalErrorHandler(misc.Memory, "out of outgoing slots", "insert_pkts")
	//return "out of outgoing slots"
}

func delete_pkt(in_pkt_id int, in_qos int) error {
	relcom_mutex.Lock()
	defer relcom_mutex.Unlock()

	var i int
	for i = 0; i < MAX_OUTGOING_PKT; i++ {
		if outgoing[i] == nil {
			continue
		}

		if outgoing[i].GetId() != in_pkt_id {
			continue
		}

		log.Printf("found %d, ", in_pkt_id)

		if outgoing[i].Get_QOS() == in_qos {
			log.Printf("same QoS %d, kill\n", in_qos)
			outgoing[i] = nil
			return nil
		}

		if outgoing[i].Get_QOS() == in_qos+1 {
			log.Printf("-1 QoS %d, count\n", in_qos)
			outgoing[i].IncAckCount()

			if outgoing[i].GetAckCount() >= MIN_LOW_QOS_ACK {
				log.Printf("enough acks, kill\n")
				outgoing[i] = nil
			}

			relcom_mutex.Unlock()
			return nil
		}

	}

	log.Printf("not found %d\n", in_pkt_id)
	return nil // actualy is possible and ok
}

func resend_pkts() {
	relcom_mutex.Lock()
	defer relcom_mutex.Unlock()

	var i int
	for i = 0; i < MAX_OUTGOING_PKT; i++ {
		if outgoing[i] == nil {
			continue
		}

		log.Printf("resend %d\n", outgoing[i].GetId())
		var rc = build_and_send(*outgoing[i])
		if rc != nil {
			misc.DetailedGlobalErrorHandler(misc.IO, rc, "resend error", "resend_pkts")
		}

		outgoing[i].IncResendCount()
		if outgoing[i].GetResendCount() >= MAX_RESEND_COUNT {
			log.Printf("too many resends for %d, kill\n", outgoing[i].GetId())
			outgoing[i] = nil
		}
	}

}
