package main

import (
	"fmt"
	"time"

	"dz.ru/mqttudp"
)

type myServer struct {
}

func main() {

	fmt.Println("MQTT/UDP passive remote config server start")

	var s myServer

	mqttudp.AddConfigServerItem("$SYS/conf/244bfe930fb0/node/location", "Kitchen")
	mqttudp.AddConfigServerItem("$SYS/conf/244bfe930fb0/node/name", "Music Box")

	mqttudp.SubServer(s)

	mqttudp.StartConfigServer()
	mqttudp.ConfigServerSendAll()

	/* for {
		fmt.Println("Send MQTT/UDP packet")
		mqttudp.Publish("fromGoLang", "hello world")
		time.Sleep(2 * time.Second)
	} */

	time.Sleep(2 * time.Hour)

}

func (s myServer) Accept(packet mqttudp.MqttPacket) {
	//fmt.Println("got pkt ", packet)
	packet.Dump()
}
