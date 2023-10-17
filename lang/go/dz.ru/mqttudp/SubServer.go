package mqttudp

import (
	"log"
	"net"

	//"github.com/projecthunt/reuseable"
	"github.com/libp2p/go-reuseport"
)

func SubServer(acceptor MqttUdpInput) {
	go listenUdp(acceptor)
}

func listenUdp(acceptor MqttUdpInput) {
	//udpServer, err := net.ListenPacket("udp", ":1883")
	udpServer, err := reuseport.ListenPacket("udp", ":1883")
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
	//fmt.Println("Got UDP pkt from", *addr)
	ParseAndProcess(data, addr, acceptor)
}
