package mqttudp

import (
	"log"
	"net"
)

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
		buf := make([]byte, PKT_BUF_SIZE)
		len, addr, err := udpServer.ReadFrom(buf)
		process(&addr, buf[0:len], acceptor)
		if err != nil {
			continue
		}
	}

}

func process(addr *net.Addr, data []byte, acceptor MqttUdpInput) {
	Parse_any_pkt(data, addr, acceptor)
}
