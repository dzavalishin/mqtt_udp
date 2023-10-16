package proto_test

import (
	"fmt"
	"mqttUdp/proto"

	"testing"

	"github.com/stretchr/testify/assert"
)

func TestPlain(t *testing.T) {

	var tf = "aaa/ccc/bbb"
	fmt.Printf("\ttest Plain ")

	assert.True(t, proto.MatchTopic(tf, "aaa/ccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/c/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/ccccc/ccc"))

	fmt.Printf("PASSED\n")
}

func TestPlus(t *testing.T) {
	var tf = "aaa/+/bbb"
	fmt.Printf("\ttest Plus ")

	assert.True(t, proto.MatchTopic(tf, "aaa/ccc/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/c/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/ccccc/ccc"))

	fmt.Printf("PASSED\n")
}

func TestSharp(t *testing.T) {
	var tf = "aaa/#"
	fmt.Printf("\ttest Sharp ")

	assert.True(t, proto.MatchTopic(tf, "aaa/ccc/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/c/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aba/ccccc/ccc"))

	fmt.Printf("PASSED\n")
}
