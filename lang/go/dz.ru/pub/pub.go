package main

import (
	"fmt"
	"os"

	"dz.ru/mqttudp"
)

func main() {

	if len(os.Args) != 3 {
		fmt.Println("Usage: pub topic value")
		os.Exit(1)
	}

	topic := os.Args[1]
	value := os.Args[2]

	mqttudp.Publish(topic, value)

	fmt.Printf("MQTT/UDP packet '%s'='%s' sent", topic, value)

}
