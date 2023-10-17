package mqttudp

type Listener func(pkt MqttPacket) error

// List of callbacks to call for incoming packets.
type listeners_list struct {
	next     *listeners_list // Next list element or 0.
	listener Listener        // Callback to call for incoming packet.
}

var listeners *listeners_list = nil

/*
Register one more listener to get incoming packets.

Used by mqtt_udp lib itself to connect subsystems.

listener Callback to call when packet arrives.
*/
func AddPacketListener(listener Listener) {
	var lp *listeners_list = new(listeners_list)

	lp.next = listeners
	lp.listener = listener

	listeners = lp
}

// Pass packet to all listeners
func (pkt MqttPacket) call_packet_listeners() {
	// Send default replies first
	recv_reply(&pkt)

	var lp *listeners_list
	for lp = listeners; lp != nil; lp = lp.next {
		//int rc =
		lp.listener(pkt) // TODO rc
		//if( rc ) break;
	}
}

/*
Default packet processing, called from Parse_any_pkt()

Reply to ping

	@todo Reply to SUBSCRIBE? Not sure.
	@todo Reply with PUBACK for PUBLISH with QoS
*/
func recv_reply(pkt *MqttPacket) {

	switch pkt.packetType {
	case PINGREQ:
		// TODO err check
		PingResponce()
		break
	//case PTYPE_SUBSCRIBE:
	//case PTYPE_PUBLISH:
	default:
		break
	}
}
