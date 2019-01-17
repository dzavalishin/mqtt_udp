package ru.dz.mqtt_udp.proto;

import ru.dz.mqtt_udp.util.MqttUdpRuntimeException;

public class TTR_Invalid extends TaggedTailRecord {

	protected TTR_Invalid(byte tag, int rawLngth) {
		super(tag, rawLngth);
	}

	@Override
	public byte[] toBytes() {
		throw new MqttUdpRuntimeException(String.format( "toBytes called for TTR_Invalid"));
	}

}
