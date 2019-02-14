package ru.dz.mqtt_udp;

import java.util.AbstractCollection;
import java.util.ArrayList;

import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.proto.TTR_ReplyTo;
import ru.dz.mqtt_udp.proto.TaggedTailRecord;
import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public class PubAckPacket extends GenericPacket {

	private PublishPacket replyTo;
	private int qos;

	/**
	 * Construct from incoming UDP data. 
	 * @param raw Data from UDP packet, starting after packet type and length.
	 * @param flags Flags from packet header.
	 * @param from Source IP address.
	 */

	public PubAckPacket(byte[] raw, byte flags, IPacketAddress from) {
		super(from);
		this.flags = flags;
		//this.from = from;
		if( raw.length > 0 )
			System.err.println("non-empty PubAck Packet");
	}

	/**
	 * Create packet to be sent.
	 */
	public PubAckPacket(PublishPacket replyTo, int qos) {
		this.replyTo = replyTo;
		this.qos = qos;

		setQoS(qos);
	}


	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacket#toBytes()
	 */
	@Override
	public byte[] toBytes() {
		byte[] pkt = new byte[0];
		AbstractCollection<TaggedTailRecord> ttrs = new ArrayList<TaggedTailRecord>();

		if(!replyTo.getPacketNumber().isPresent())
		{
			GlobalErrorHandler.handleError(ErrorType.Protocol, "attempt to PubAck for pkt with no id");
			//throw new MqttProtocolException("attempt to PubAck for pkt with no id");
		}
		else
		{
			TTR_ReplyTo id = new TTR_ReplyTo(replyTo.getPacketNumber().get());
			ttrs.add(id);
		}
		
		return IPacket.encodeTotalLength(pkt, mqtt_udp_defs.PTYPE_PUBACK, flags, ttrs );	
	}

	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacket#getType()
	 */
	@Override
	public int getType() {		return mqtt_udp_defs.PTYPE_PUBACK;	}

	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.util.GenericPacket#toString()
	 */
	@Override
	public String toString() {		
		return String.format("MQTT/UDP PubAck" );
	}	

}
