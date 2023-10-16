package proto_test

import (
	//"log"
	"mqttUdp/proto"
	"testing"
)

func Test_ttr_decode_int32(t *testing.T) {
	var raw = []byte{0x30, 8, 0, 4, 'a', 'b', 'c', 'd', 'x', 'y'}

	proto.Parse_any_pkt(raw, nil, nil)
}
