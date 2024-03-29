package mqttudp

import (
	"log"
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

// -----------------------------------------------------------------------
//
// Interface
//
// -----------------------------------------------------------------------

/*
Compose and send PUBLISH packet.

@param topic  Message topic
@param data   Message value, usually text string
*/
func send_publish_qos(topic []byte, data []byte, qos int) error {

	var pp MqttPacket = NewMqttPacket(PUBLISH, topic, data)

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

func Process(pkt MqttPacket) error {
	if pkt.GetType() == PUBACK {
		log.Printf("got ack to %d\n", pkt.GetReplyTo())
		if pkt.GetReplyTo() == 0 {
			GlobalErrorHandler(Proto, "puback reply_to 0", "relcom_listener")
			return nil
		}
		delete_pkt(pkt.GetReplyTo(), pkt.Get_QOS())
	}

	return nil
}

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

func init() {
	//ARCH_MUTEX_INIT(relcom_mutex)
	//log.Println("RelCom init")
	AddPacketListener(Process)
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
// Outgoing list
//
// -----------------------------------------------------------------------

// Naive fixed size array impl
const MAX_OUTGOING_PKT = 30
const MIN_LOW_QOS_ACK = 2
const MAX_RESEND_COUNT = 3

var outgoing []*MqttPacket = make([]*MqttPacket, MAX_OUTGOING_PKT)

func insert_pkt(pp MqttPacket) error {
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

	return GlobalErrorHandler(Memory, "out of outgoing slots", "insert_pkts")
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
			DetailedGlobalErrorHandler(IO, rc, "resend error", "resend_pkts")
		}

		outgoing[i].IncResendCount()
		if outgoing[i].GetResendCount() >= MAX_RESEND_COUNT {
			log.Printf("too many resends for %d, kill\n", outgoing[i].GetId())
			outgoing[i] = nil
		}
	}

}
