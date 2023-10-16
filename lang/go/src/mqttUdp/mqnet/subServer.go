package mqnet

import (
	"log"
	"mqttUdp/proto"
	"net"
)

func SubServer(acceptor proto.MqttUdpInput) {
	go listenUdp(acceptor)
}

func listenUdp(acceptor proto.MqttUdpInput) {
	udpServer, err := net.ListenPacket("udp", ":1053")
	if err != nil {
		log.Fatal(err)
	}
	defer udpServer.Close()

	for {
		buf := make([]byte, 1024)
		len, addr, err := udpServer.ReadFrom(buf)
		process(&addr, buf[0:len], acceptor)
		if err != nil {
			continue
		}
	}

}

func process(addr *net.Addr, data []byte, acceptor proto.MqttUdpInput) {
	proto.Parse_any_pkt(data, addr, acceptor)
}
