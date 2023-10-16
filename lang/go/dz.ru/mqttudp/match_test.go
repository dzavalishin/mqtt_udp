package mqttudp

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestPlain(t *testing.T) {

	var tf = "aaa/ccc/bbb"
	fmt.Printf("\ttest Plain ")

	assert.True(t, MatchTopic(tf, "aaa/ccc/bbb"))
	assert.False(t, MatchTopic(tf, "aaa/c/bbb"))
	assert.False(t, MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, MatchTopic(tf, "aaa/ccccc/ccc"))

	fmt.Printf("PASSED\n")
}

func TestPlus(t *testing.T) {
	var tf = "aaa/+/bbb"
	fmt.Printf("\ttest Plus ")

	assert.True(t, MatchTopic(tf, "aaa/ccc/bbb"))
	assert.True(t, MatchTopic(tf, "aaa/c/bbb"))
	assert.True(t, MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, MatchTopic(tf, "aaa/ccccc/ccc"))

	fmt.Printf("PASSED\n")
}

func TestSharp(t *testing.T) {
	var tf = "aaa/#"
	fmt.Printf("\ttest Sharp ")

	assert.True(t, MatchTopic(tf, "aaa/ccc/bbb"))
	assert.True(t, MatchTopic(tf, "aaa/c/bbb"))
	assert.True(t, MatchTopic(tf, "aaa/ccccc/bbb"))
	assert.False(t, MatchTopic(tf, "aba/ccccc/ccc"))

	fmt.Printf("PASSED\n")
}
