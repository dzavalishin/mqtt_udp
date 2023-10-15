package proto

/*
type ITaggedTailRecord interface {
	//getRawLength() int
	getRawData() []byte
	getTag() byte
}

func toBytes(r ITaggedTailRecord) []byte {
	var raw = r.getRawData()
	var len = len(raw)

	if (len > 0x7F) || (len < 0) {
		return nil
	}

	var out []byte
	out = make([]byte, len+2)

	out[0] = r.getTag()
	out[1] = (byte)(len & 0x7F)

	copy(out[2:], raw)

	return out
}

func newTaggedTailRecord(raw []byte) *ITaggedTailRecord {
	var rawLength int = 1
	var tag byte = raw[0]

	var dlen int = 0
	var pos int = 1

	for {
		rawLength++

		var b byte
		b = raw[pos]
		pos++

		dlen |= (int(b)) & ^0x80

		if (b & 0x80) == 0 {
			break
		}

		dlen <<= 7
	}

	rawLength += dlen

	var rec []byte
	rec = make([]byte, dlen)

	//System.arraycopy(raw, pos, rec, 0, dlen);
	copy(rec, raw[pos:pos+dlen])

	return decodeRecord(tag, rec, rawLength)
}

func decodeRecord(tag byte, rec []byte, rawLength int) *ITaggedTailRecord {
	switch tag {
	case 'n':
		return newTTR_PacketNumber(tag, rec, rawLength)
	case 'r':
		return newTTR_ReplyTo(tag, rec, rawLength)
	case 's':
		return newTTR_Signature(tag, rec, rawLength)
	default:
		break
	}

	return newTTR_Invalid(tag, rawLength)
}

type AbstractTaggedTailRecord struct {
	rawLength int
	tag       byte
}

func (r AbstractTaggedTailRecord) getTag() byte {
	return r.tag
}

type AbstractInt32TaggedTailRecord struct {
	AbstractTaggedTailRecord
	value int
}

func (r AbstractInt32TaggedTailRecord) getRawData() []byte {
	var out []byte
	out = make([]byte, 4)
	var i int
	for i = 0; i < 4; i++ {
		out[i] = (byte)(r.value >> (8 * (4 - i - 1)))
	}

	return out
}

type TTR_PacketNumber AbstractInt32TaggedTailRecord

func newTTR_Invalid(tag byte, rawLength int) *ITaggedTailRecord {
	// TODO
	return nil
}

func newTTR_PacketNumber(tag byte, rec []byte, rawLength int) *ITaggedTailRecord {
	// TODO
	var me *TTR_PacketNumber
	me = new(TTR_PacketNumber)

	return me
}

func newTTR_Signature(tag byte, rec []byte, rawLength int) *ITaggedTailRecord {
	// TODO
	return nil
}

func newTTR_ReplyTo(tag byte, rec []byte, rawLength int) *ITaggedTailRecord {
	// TODO
	return nil
}
*/
