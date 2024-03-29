package mqttudp

import (
	"log"
	"net"
)

var udpSend *net.UDPConn = nil

func getSendSocket() *net.UDPConn {
	if udpSend == nil {
		//var u *net.UDPConn = nil

		addr := net.UDPAddr{
			Port: MQTT_PORT,
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

	Throttle() // Speed limit

	/*addr := net.UDPAddr{
		Port: MQTT_PORT,
		IP:   net.ParseIP("255.255.255.255"),
	}*/

	//fmt.Println("Sending pkt ", data[0:len])

	//_, err := conn.WriteToUDP(data[0:len], nil) //&addr)
	_, err := conn.Write(data[0:len])
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

// -----------------------------------------------------------------------
//
// Send
//
// -----------------------------------------------------------------------

func build_and_send(pp MqttPacket) error {
	var buf []byte = make([]byte, PKT_BUF_SIZE)
	var out_size int

	pp.Dump()

	var rc error
	out_size, rc = pp.BuildAnyPkt(buf)
	if rc != nil {
		return rc
	}

	//mqtt_udp_dump( buf, out_size );

	return send_pkt(buf, out_size)
}
