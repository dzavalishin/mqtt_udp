package ru.dz.mqtt_udp.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import ru.dz.mqtt_udp.io.IPacketAddress;

/**
 * Packets with topic field.
 * Support ifTopicIs filtering.
 * @author dz
 *
 */
public abstract class TopicPacket extends GenericPacket {

	/**
	 * Packet from net.
	 * @param from Source address.
	 */
	public TopicPacket(IPacketAddress from) {
		super(from);
	}

	/**
	 * Local packet.
	 */
	public TopicPacket() {
	}

	
	protected String  topic;
	/**
	 * Get topic value.
	 * @return Topic string.
	 */
	public String getTopic() {			return topic;	}
	
	/**
	 * Send me to sink if predicate is true. Packet filtering.
	 * @param p Predicate to check.
	 * @param c Sink to consume me if predicate is true.
	 */
	public void ifTopicIs( Predicate<TopicPacket> p, Consumer<TopicPacket> c)
	{
		if( p.test(this)) c.accept(this);
	}

}
