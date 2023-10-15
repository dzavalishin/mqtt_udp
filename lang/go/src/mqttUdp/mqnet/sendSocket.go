package mqnet

import (
	"log"
	"net"
	"os"
)

var udpSend *net.UDPAddr = nil

func getSendSocket() *net.UDPAddr {
	if udpSend == nil {
		var u *net.UDPAddr = nil
		u, err := net.ResolveUDPAddr("udp", ":1883") // TODO port def

		if err != nil {
			log.Println("ResolveUDPAddr failed:", err.Error())
			os.Exit(1) // TODO exit
		} else {
			udpSend = u
		}

	}

	return udpSend
}
