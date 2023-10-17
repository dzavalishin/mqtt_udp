/**
 *
 * MQTT/UDP project
 *
 * https://github.com/dzavalishin/mqtt_udp
 *
 * Copyright (C) 2017-2023 Dmitry Zavalishin, dz@dz.ru
 *
 * Passive remote configuration.
 *
 * This code represents side that is being configured remotely.
 *
 * Device keeps configuration items locally in file/flash/nvram.
 * Configuration software (for example, /tools/viewer) requests
 * list of configurable topics (SUBSCRIBE $SYS/#) and device
 * responds with PUBLISH for all configuration topics. Config
 * software then can set config parameters values with PUBLISH.
 *
 * @see ru.dz.mqtt_udp.config.Controller Java class for reference
 *
**/

package mqttudp

import (
	"errors"
	"fmt"
	"log"
	"net"
	"strings"
)

type RConfigCallback func(pos int, write bool)

var rconfig_list []RConfigItem
var user_rw_callback RConfigCallback
var rconfig_mac_address_string string
var topic_prefix string

/*
Called from user code to setup remote configuration.

@param mac_address_string   MAC address of current device (packed: "020698010000") or other unique id for current node.
@param cb                   Callback function to call when remote config engine needs parameter to be loaded or saved.
@param rconfig_items        Pointer to array of configurable items.
*/
func RConfigClientInit(cb RConfigCallback, rconfig_items []RConfigItem) error {

	mac_address_string, err := getMacAddr()
	if err != nil {
		return err
	}

	log.Printf("RConfig client init with mac '%s'\n", mac_address_string)

	rconfig_list = rconfig_items

	user_rw_callback = cb

	rconfig_mac_address_string = mac_address_string

	mac_len := len(rconfig_mac_address_string)
	// TODO do we need it?
	if mac_len > 24 {
		return GlobalErrorHandler(Memory, "mac str too long", rconfig_mac_address_string)
	}

	//slog.Printf( topic_prefix, "$SYS/%s/conf/", rconfig_mac_address_string );
	topic_prefix = fmt.Sprintf(SYS_CONF_PREFIX+"/%s/", rconfig_mac_address_string)

	//log.Printf("topic_prefix_len %d len %d\n", topic_prefix_len, strlen(topic_prefix));

	AddPacketListener(rconfig_listener)

	rconfig_read_all()
	rconfig_send_topic_list()

	return nil
}

/**
 *
 * @brief Set value of configuration parameter with string input.
 *
 * @todo Convert for numeric/boolean parameters.
 *
 * @param pos     Position in items array to set value for.
 * @param str  New parameter value.
 *
 * @return 0 on success, or error code.
 *
**/
func RConfigSetString(pos int, str string) error {
	if (pos < 0) || (pos >= cap(rconfig_list)) {
		return GlobalErrorHandler(Invalid, "pos out of list", str)
	}

	if rconfig_list[pos].itype != MQ_CFG_TYPE_STRING {
		return GlobalErrorHandler(Invalid, "!string", str)
	}

	if rconfig_list[pos].kind == MQ_CFG_KIND_INFO {
		return GlobalErrorHandler(Invalid, "R/O", str)
	}

	rconfig_list[pos].s = str

	return nil
}

/*
Process incoming packets.

Process PUBLISH and SUBSCRIBE requests for config items.

@param pkt Packet to process.
*/
func rconfig_listener(pkt MqttPacket) error {

	//log.Printf("rconf\n")

	// Got request
	if pkt.GetType() == SUBSCRIBE {
		// is `$SYS/#` or `$SYS/conf/#` or `$SYS/conf/{our MAC}/`
		//if( 0 == strcmp( pkt.topic, SYS_WILD ) ) { rconfig_send_topic_list(); return 0; }
		if MatchTopic(string(pkt.topic), topic_prefix) {
			log.Printf("RCONF send all\n")
			rconfig_send_topic_list()
			return nil
		}

		pos := find_by_full_topic(string(pkt.topic))
		if pos < 0 {
			return nil
		}
		log.Printf("RCONF got subscribe '%s' pos = %d\n", pkt.topic, pos)
		rconfig_send_topic_by_pos(pos)
	}

	// Got data
	if pkt.GetType() == PUBLISH {
		pos := find_by_full_topic(string(pkt.topic))
		if pos < 0 {
			return nil
		}

		log.Printf("RCONF set '%s'='%s' pos = %d\n", pkt.topic, pkt.value, pos)

		rc := RConfigSetString(pos, string(pkt.value))
		if rc != nil {
			DetailedGlobalErrorHandler(Other, rc, "RConfigSetString failed", string(pkt.value))
		}

		//rc =
		user_rw_callback(pos, true) // Ask user to write item to local storage and use it

	}

	return nil
}

/**
 *
 * @brief Find config item number (position in array) by full incoming topic name.
 *
 * Topic name must include "$SYS/{MAC address}/conf/" prefix.
 *
 * @param topic   Full topic name to parse and find.
 *
 * @return Position in array or negative error code.
 *
**/
func find_by_full_topic(topic string) int {

	//log.Printf("topic  '%s'\n", topic );

	//log.Printf("prefix '%s'\n", topic_prefix );
	topic_prefix_len := len(topic_prefix)
	topic_len := len(topic)

	//if strncmp(topic_prefix, topic, topic_prefix_len) {		return -1	}
	if topic_len < topic_prefix_len || topic_prefix == topic[0:topic_prefix_len] {
		return -1
	}

	//const char *suffix = topic + topic_prefix_len
	suffix := topic[topic_prefix_len:]

	return rconfig_find_by_topic(suffix)
}

/**
 *
 * @brief Send out current value for configuration item.
 *
 * @param pos Configuration item position in array.
 *
**/
func rconfig_send_topic_by_pos(pos int) {

	log.Printf("rconfig_send_topic_by_pos %d: ", pos)

	// TODO do more
	if rconfig_list[pos].itype != MQ_CFG_TYPE_STRING {
		return
	}

	subtopic := rconfig_list[pos].topic

	//char topic[80];
	//snlog.Printf( topic, sizeof(topic)-1, "$SYS/%s/conf/%s", rconfig_mac_address_string, subtopic );
	topic := fmt.Sprintf(SYS_CONF_PREFIX+"/%s/%s", rconfig_mac_address_string, subtopic)

	val := rconfig_list[pos].s

	Publish(topic, val)

	log.Printf("RCONF publish '%s'='%s'\n", topic, val)
}

/**
 *
 * @brief Send out current value for all configuration items.
 *
**/
func rconfig_send_topic_list() {
	for i := 0; i < len(rconfig_list); i++ {
		rconfig_send_topic_by_pos(i)
	}
}

/**
 *
 * @brief Find config item number (position in array) by short topic name (end of full name).
 *
 * Topic name must NOT include "$SYS/{MAC address}/conf/" prefix. Just final part.
 *
 * @param topic  Topic name suffix to find.
 *
 * @return Item position in array or -1 if not found.
 *
**/
func rconfig_find_by_topic(topic string) int {
	for i := 0; i < len(rconfig_list); i++ {
		if rconfig_list[i].topic == topic {
			return i
		}
	}

	return -1
}

/**
 *
 * @brief Request all items values from user code.
 *
 *
**/
func rconfig_read_all() {
	for i := 0; i < len(rconfig_list); i++ {
		user_rw_callback(i, false) // Ask user to read item from local storage
	}
}

// -----------------------------------------------------------------------
//
// Node ID
//
// -----------------------------------------------------------------------

func getMacAddr() (string, error) {
	ifas, err := net.Interfaces()
	if err != nil {
		return "", err
	}

	for _, ifa := range ifas {
		a := ifa.HardwareAddr.String()
		if a != "" {
			return strings.ReplaceAll(a, ":", ""), nil
		}
	}
	return "", errors.New("no MAC address?")
}

// -----------------------------------------------------------------------
//
// Helpers for user to work with rconfig_list
//
// Not used in lib and not required to use
//
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
//
// Topic to index and back
//
// -----------------------------------------------------------------------

/**
 *
 * @brief Find config item number (position in array) by string value.
 *
 * Used to find io channel number by topic name. Remote config item
 * supposed to contain topic name.
 *
 * It is supposed that item index is equal to io channel nubmer.
 * Usually it means that topic related items are at the beginning
 * of item array and their position in array is important.
 *
 * @param search  String (topic name?) to find in item _value_.
 *
 * @param kind    Expected kind og the item, sanity check.
 *
 * @return Item position in array or -1 if not found.
 *
**/
func RConfigFindByStringValue(search string, kind RConfigItemKind) int {
	for i := 0; i < len(rconfig_list); i++ {
		if rconfig_list[i].itype != MQ_CFG_TYPE_STRING {
			continue
		}

		if rconfig_list[i].kind != kind {
			continue
		}

		if rconfig_list[i].s == search {
			return i
		}
	}

	return -1
}

/*
Get config item string by item number (position in array).

Used to find topic for io channel by channel number.
supposed to contain topic name

It is supposed that item index is equal to io channel nubmer.
Usually it means that topic related items are at the beginning
of item array and their position in array is important.

@todo Convert other types to string?

@param pos Position in array.

@param kind Expected kind og the item, sanity check.

@return Item _value_ (string) or 0 if intem type is not string.
*/
func RConfigGetStringByItemIndex(pos int, kind RConfigItemKind) (string, bool) {
	if rconfig_list[pos].itype != MQ_CFG_TYPE_STRING {
		GlobalErrorHandler(Invalid, "string_by_item_index !str", "rconfig_get_string_by_item_index")
		return "", false
	}

	if rconfig_list[pos].kind != kind {
		GlobalErrorHandler(Invalid, "string_by_item_index !kind", "rconfig_get_string_by_item_index")
		return "", false
	}

	return rconfig_list[pos].s, true
}

// -----------------------------------------------------------------------
//
// RConfigItem methods
//
// -----------------------------------------------------------------------

func NewTopicRConfigItem(name, topic, value, opaque string) RConfigItem {
	var ret RConfigItem

	ret.itype = MQ_CFG_TYPE_STRING
	ret.kind = MQ_CFG_KIND_TOPIC

	ret.name = name
	ret.topic = topic
	ret.s = value
	ret.Opaque = opaque

	return ret
}

func NewOtherRConfigItem(name, topic, value, opaque string) RConfigItem {
	var ret RConfigItem

	ret.itype = MQ_CFG_TYPE_STRING
	ret.kind = MQ_CFG_KIND_OTHER

	ret.name = name
	ret.topic = topic
	ret.s = value
	ret.Opaque = opaque

	return ret
}

func NewInfoRConfigItem(name, topic, value, opaque string) RConfigItem {
	var ret RConfigItem

	ret.itype = MQ_CFG_TYPE_STRING
	ret.kind = MQ_CFG_KIND_INFO

	ret.name = name
	ret.topic = topic
	ret.s = value
	ret.Opaque = opaque

	return ret
}

func (i RConfigItem) GetType() RConfigItemType {
	return i.itype
}

func (i RConfigItem) GetKind() RConfigItemKind {
	return i.kind
}

func (i RConfigItem) GetTopic() string {
	return i.topic
}

func (i RConfigItem) GetStringValue() string {
	return i.s
}

func (i RConfigItem) SetStringValue(s string) {
	i.s = s
}
