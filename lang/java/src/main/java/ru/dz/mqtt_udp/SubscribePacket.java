package ru.dz.mqtt_udp;

import ru.dz.mqtt_udp.util.NoEncodingRuntimeException;
import ru.dz.mqtt_udp.util.TopicPacket;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public class SubscribePacket extends TopicPacket {


	/**
	 * Construct from incoming UDP data. 
	 * @param raw Data from UDP packet, starting after packet type and length.
	 * @param flags Flags from packet header.
	 * @param from Source IP address.
	 */

	public SubscribePacket(byte[] raw, byte flags, IPacketAddress from) {
		super(from);
		this.flags = flags;
		int tlen = IPacket.decodeTopicLen( raw );

		topic = new String(raw, 2, tlen, Charset.forName(MQTT_CHARSET));
		// TODO byte of QoS - do we need it?
	}



	/**
	 * Create packet to be sent.
	 * @param topic Topic string.
	 * @param flags Protocol flags.
	 */
	public SubscribePacket(String topic, byte flags) {
		super(null);
		makeMe( topic, flags );
	}

	/**
	 * Create packet to be sent.
	 * @param topic Topic string.
	 */
	public SubscribePacket(String topic) {
		super(null);
		makeMe( topic, (byte) 0 );
	}


	private void makeMe(String topic, byte flags) {
		this.topic = topic;
		this.flags = flags;
	}



	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacket#toBytes()
	 */
	@Override
	public byte[] toBytes() {
		byte[] tbytes;
		try {
			tbytes = topic.getBytes(MQTT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new NoEncodingRuntimeException(e);
		}

		int plen = tbytes.length + 2 + 1; // + QoS byte

		byte [] pkt = new byte[plen]; 

		pkt[0] = (byte) (((tbytes.length >>8) & 0xFF) | (flags & 0x0F)); // TODO encodeTotalLength does it?
		pkt[1] = (byte) (tbytes.length & 0xFF);

		System.arraycopy(tbytes, 0, pkt, 2, tbytes.length);
		//System.arraycopy(value, 0, pkt, tbytes.length + 2, value.length );

		pkt[tbytes.length + 2] = 0; // Requested QoS is allways zero now - TODO add property
		
		return IPacket.encodeTotalLength(pkt, mqtt_udp_defs.PTYPE_SUBSCRIBE, flags, null, this );
	}

	@Override
	public String toString() {		
		return String.format("MQTT/UDP SUBSCRIBE '%s'", getTopic() );
	}

	
	//public void setFlags(byte flags) {		this.flags = flags;	}

	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacket#getType()
	 */
	@Override
	public int getType() {
		return mqtt_udp_defs.PTYPE_SUBSCRIBE;
	}

}
