package mqttudp

type PType int

// TODO must be generated

const MQTT_PORT = 1883

const (
	CONNECT     PType = 0x10
	CONNACK     PType = 0x20
	PUBLISH     PType = 0x30
	PUBACK      PType = 0x40
	PUBREC      PType = 0x50
	PUBREL      PType = 0x60
	PUBCOMP     PType = 0x70
	SUBSCRIBE   PType = 0x80
	SUBACK      PType = 0x90
	UNSUBSCRIBE PType = 0xA0
	UNSUBACK    PType = 0xB0
	PINGREQ     PType = 0xC0
	PINGRESP    PType = 0xD0
	DISCONNECT  PType = 0xE0
)

const PKT_BUF_SIZE = 4096
const MAX_SZ = PKT_BUF_SIZE - 2

// -----------------------------------------------------------------------
// Signature
// -----------------------------------------------------------------------

const MD5_DIGEST_SIZE = 16
const SIGNATURE_TTR_SIZE = MD5_DIGEST_SIZE + 2 // Signature TTR needs this many bytes

// -----------------------------------------------------------------------
// Remote config
// -----------------------------------------------------------------------

const SYS_CONF_PREFIX = "$SYS/conf"

// Types of configuration items
type RConfigItemType int

const (
	MQ_CFG_TYPE_BOOL   RConfigItemType = 1
	MQ_CFG_TYPE_STRING RConfigItemType = 2
	MQ_CFG_TYPE_INT32  RConfigItemType = 3
)

/*
Kinds of configuration items.

Used by host-faced code for internal processing.
Does not affect network communications.
*/
type RConfigItemKind int

const (
	MQ_CFG_KIND_OTHER RConfigItemKind = 0
	MQ_CFG_KIND_TOPIC RConfigItemKind = 1 // Topics device works with, R/W
	MQ_CFG_KIND_INFO  RConfigItemKind = 2 // Read-Only
	MQ_CFG_KIND_NODE  RConfigItemKind = 3 // Node info, R/W
)

type RConfigItem struct {
	itype RConfigItemType // Item (.value field) data type (string, bool, number, other)
	kind  RConfigItemKind // Item kind, not processed by network code
	name  string          // Human readable name for this config parameter
	topic string          // MQTT/UDP topic name for this config parameter
	//mqtt_udp_rconfig_item_value_t       value;  ///< Current value
	//mqtt_udp_rconfig_item_value_t       opaque; ///< user data item, not processed by MQTT/UDP code at all
	s      string // Current value
	Opaque string
}
