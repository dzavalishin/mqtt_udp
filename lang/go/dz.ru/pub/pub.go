package main

import (
	"flag"
	"fmt"
	"os"

	"dz.ru/mqttudp"
)

func main() {

	var pass string

	flag.StringVar(&pass, "s", "", "Specify signature password")
	flag.Parse() // after declaring flags we need to call it

	if flag.NArg() != 2 {
		fmt.Println("Usage: pub [-s password] topic value")
		os.Exit(1)
	}

	topic := flag.Arg(1)
	value := flag.Arg(2)

	if len(pass) > 0 {
		mqttudp.EnableSignature([]byte(pass))
	}

	mqttudp.Publish(topic, value)

	fmt.Printf("MQTT/UDP packet '%s'='%s' sent", topic, value)

}
