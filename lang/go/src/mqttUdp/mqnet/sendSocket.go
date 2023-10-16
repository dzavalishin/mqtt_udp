package mqnet

import (
	"log"
	"mqttUdp/proto"
	"net"
)

var udpSend *net.UDPConn = nil

func getSendSocket() *net.UDPConn {
	if udpSend == nil {
		//var u *net.UDPConn = nil

		addr := net.UDPAddr{
			Port: 1883, // TODO const
			IP:   net.ParseIP("255.255.255.255"),
		}

		u, err := net.DialUDP("udp", nil, &addr)
		if err != nil {
			log.Printf("net.Dial failed %v", err)
			return nil
		} else {
			udpSend = u
		}

	}

	return udpSend
}

func send_pkt(data []byte, len int) error {
	return send_pkt_fd(getSendSocket(), data, len)
}

func send_pkt_fd(conn *net.UDPConn, data []byte, len int) error {

	proto.Throttle() // Speed limit

	addr := net.UDPAddr{
		Port: 1883,
		IP:   net.ParseIP("255.255.255.255"),
	}

	_, err := conn.WriteToUDP(data[0:len], &addr)
	if err != nil {
		log.Printf("Couldn't send udp %v", err)
	}

	return err
}

/*
func send_pkt_addr( int fd, char *data, size_t len, uint32_t ip_addr ) error
{
    //struct sockaddr_in addr;

    struct sockaddr_in serverAddr;
    socklen_t addr_size;

    //Configure settings in address struct
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons( MQTT_PORT );
    serverAddr.sin_addr.s_addr = ip_addr;
    memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);

    addr_size = sizeof serverAddr;

    ssize_t rc = sendto( fd, data, len, 0, (struct sockaddr *)&serverAddr, addr_size);

    return (rc != len) ? EIO : 0;
} */
