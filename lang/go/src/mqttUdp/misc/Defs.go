package misc

type PType int

// TODO must be generated

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
