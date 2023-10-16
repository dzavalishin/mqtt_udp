package proto_test

import (
	"mqttUdp/proto"

	"testing"

	"github.com/stretchr/testify/assert"
)

func TestPlain(t *testing.T) {

	var tf = "aaa/ccc/bbb"
	//printf("\ttest Plain ");

	assert.True(t, proto.MatchTopic(tf, "aaa/ccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/c/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/ccccc/ccc"))

}

func TestPlus(t *testing.T) {
	var tf = "aaa/+/bbb"
	//printf("\ttest Plus ")
	assert.True(t, proto.MatchTopic(tf, "aaa/ccc/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/c/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aaa/ccccc/ccc"))
	//printf("PASSED\n")
}

func TestSharp(t *testing.T) {
	var tf = "aaa/#"
	//printf("\ttest Sharp ")
	assert.True(t, proto.MatchTopic(tf, "aaa/ccc/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/c/bbb"))
	assert.True(t, proto.MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, proto.MatchTopic(tf, "aba/ccccc/ccc"))
	//printf("PASSED\n")
}
