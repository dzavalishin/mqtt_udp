package proto

// List of callbacks to call for incoming packets.
type listeners_list struct {
	next     *listeners_list // Next list element or 0.
	listener PacketProcessor // Callback to call for incoming packet.
}

var listeners *listeners_list = nil

/*

 Register one more listener to get incoming packets.

 Used by mqtt_udp lib itself to connect subsystems.

 listener Callback to call when packet arrives.

*/
func AddPacketListener(listener PacketProcessor) {
	var lp *listeners_list = new(listeners_list)

	lp.next = listeners
	lp.listener = listener

	listeners = lp
}

// Pass packet to all listeners
func (pkt MqttPacket) call_packet_listeners() {
	var lp *listeners_list
	for lp = listeners; lp != nil; lp = lp.next {
		//int rc =
		lp.listener.Process(pkt)
		//if( rc ) break;
	}
}
