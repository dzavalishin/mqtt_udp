package main

import (
	"fmt"
	"mqttUdp/mqnet"
	"mqttUdp/proto"
)

//import "mqttUdp/net"
//import "mqttUdp/proto"

type myServer struct {
}

func main() {
	fmt.Println("Hello, World!")

	var s myServer
	//s = new(&myServer)

	mqnet.SubServer(s)

	/*
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
		} */

}

func (s myServer) Accept(packet proto.MqttPacket) {
	fmt.Println("got pkt ", packet)
}

/*
func process(addr net.Addr, data []byte) {

} */
