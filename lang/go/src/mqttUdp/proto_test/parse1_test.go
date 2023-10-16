package proto_test

import (
	//"log"

	"mqttUdp/proto"
	"testing"
)

type myServer struct {
	t *testing.T
}

func (s myServer) Accept(packet proto.MqttPacket) {
	//fmt.Println("got pkt ", packet)
	packet.Dump()
}

func Test_Parse_Publish(t *testing.T) {
	var raw = []byte{0x30, 8, 0, 4, 'a', 'b', 'c', 'd', 'x', 'y'}

	proto.Parse_any_pkt(raw, nil, nil)
}

func Test_Parse_Publish_TTR(t *testing.T) {
	var raw = []byte{0x30, 8, 0, 4, 'a', 'b', 'c', 'd', 'x', 'y' /*TTR*/, 'n', 4, 0, 0, 0, 1}

	proto.Parse_any_pkt(raw, nil, nil)
}

func Test_Parse_Publish_callback(t *testing.T) {
	var raw = []byte{0x30, 8, 0, 4, 'a', 'b', 'c', 'd', 'x', 'y'}

	var cb myServer
	cb.t = t

	proto.Parse_any_pkt(raw, nil, cb)
}

func Test_Parse_Publish_TTR_callback(t *testing.T) {
	var raw = []byte{0x30, 8, 0, 4, 'a', 'b', 'c', 'd', 'x', 'y' /*TTR*/, 'n', 4, 0, 0, 0, 1}

	var cb myServer
	cb.t = t

	proto.Parse_any_pkt(raw, nil, cb)
}
