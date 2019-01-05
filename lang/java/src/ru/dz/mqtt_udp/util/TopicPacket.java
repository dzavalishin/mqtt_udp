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

	public TopicPacket(IPacketAddress from) {
		super(from);
	}

	public TopicPacket() {
	}

	
	protected String  topic;
	public String getTopic() {			return topic;	}
	
	
	public void ifTopicIs( Predicate<TopicPacket> p, Consumer<TopicPacket> c)
	{
		if( p.test(this)) c.accept(this);
	}

}
