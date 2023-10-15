package mqnet

import (
	"log"
	"mqttUdp/proto"
	"net"
)

type MqttUdpInput interface {
	Accept(packet proto.MqttPacket)
}

func SubServer(acceptor MqttUdpInput) {
	go listenUdp(acceptor)
}

func listenUdp(acceptor MqttUdpInput) {
	udpServer, err := net.ListenPacket("udp", ":1053")
	if err != nil {
		log.Fatal(err)
	}
	defer udpServer.Close()

	for {
		buf := make([]byte, 1024)
		len, addr, err := udpServer.ReadFrom(buf)
		process(addr, buf[0:len])
		if err != nil {
			continue
		}
	}

}

func process(addr net.Addr, data []byte) {
	proto.Parse_any_pkt(data, addr)
}
