package mqnet

import "mqttUdp/proto"

type MqttUdpInput interface {
	Accept(packet proto.MqttPacket)
}

func SubServer(acceptor MqttUdpInput) {
	go listenUdp(acceptor)
}

func listenUdp(acceptor MqttUdpInput) {

}
