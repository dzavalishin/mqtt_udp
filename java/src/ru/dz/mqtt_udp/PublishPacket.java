package ru.dz.mqtt_udp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public class PublishPacket extends GenericPacket {

	private String topic;
	private byte[] value;

	public PublishPacket(byte[] raw, String from) {
		int tlen = IPacket.decodeTopicLen( raw );

		topic = new String(raw, 2, tlen, Charset.forName(MQTT_CHARSET));
		
		int vlen = raw.length - tlen - 2;		
		value = new byte[vlen];	
		System.arraycopy( raw, tlen+2, value, 0, vlen );
		
		this.from = from;
	}

	
	public String getTopic() {			return topic;	}
	public byte[] getValueRaw() {		return value;	}	
	public String getValueString() {	return new String(value, Charset.forName(MQTT_CHARSET));	}
	
	public PublishPacket(String topic, byte[] value) {
		makeMe( topic, value );
	}

	public PublishPacket(String topic, String value) {
		try {
			makeMe( topic, value.getBytes(MQTT_CHARSET) );
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void makeMe(String topic, byte[] value) {
		this.topic = topic;
		this.value = value;
	}
	
	
	
	@Override
	public byte[] toBytes() {
		byte[] tbytes;
		try {
			tbytes = topic.getBytes(MQTT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		int plen = tbytes.length + value.length + 2;
					
		byte [] pkt = new byte[plen]; 

		pkt[0] = (byte) ((tbytes.length >>8) & 0xFF);
		pkt[1] = (byte) (tbytes.length & 0xFF);
		
		System.arraycopy(tbytes, 0, pkt, 2, tbytes.length);
		System.arraycopy(value, 0, pkt, tbytes.length + 2, value.length );
		
		//return IPacket.encodeTotalLength(pkt, IPacket.PT_PUBLISH);
		return IPacket.encodeTotalLength(pkt, mqtt_udp_defs.PTYPE_PUBLISH );
	}

	@Override
	public String toString() {		
		return String.format("MQTT/UDP PUBLISH '%s'='%s'", getTopic(), getValueString() );
	}
	
	
}
