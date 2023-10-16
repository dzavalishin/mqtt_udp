package main

import (
	"fmt"
	"time"

	"dz.ru/mqttudp"
)

type myServer struct {
}

func main() {
	fmt.Println("Start listening to MQTT/UDP traffic")

	var s myServer

	mqttudp.SubServer(s)

	time.Sleep(2 * time.Hour)

}

func (s myServer) Accept(packet mqttudp.MqttPacket) {
	//fmt.Println("got pkt ", packet)
	packet.Dump()
}
