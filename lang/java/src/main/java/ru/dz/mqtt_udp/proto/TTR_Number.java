package ru.dz.mqtt_udp.proto;

public class TTR_Number extends TaggedTailRecord {

	public TTR_Number(byte tag, byte[] rec, int rawLength) {
		super(tag,rawLength);
	}

}
