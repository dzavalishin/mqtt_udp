package ru.dz.mqtt_udp.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.items.AbstractItem;
import ru.dz.mqtt_udp.items.TopicItem;

public class Requester implements Consumer<IPacket> {
	Map<String,TopicItem> items = new HashMap<>();

	public Requester(IPacketMultiSource ms) 
	{
		ms.addPacketSink(this);	
	}


	public void addTopic(String topicName)
	{
		// TODO need class PublishTopicItem?
		//items.put(topicName, new TopicItem(mqtt_udp_defs.PTYPE_PUBLISH, topicName, topicValue));
		items.put(topicName, null);
	}

	@Override
	public void accept(IPacket t) {
		if( !(t instanceof PublishPacket) ) 
			return;

		PublishPacket pp = (PublishPacket) t;

		if( !items.containsKey(pp.getTopic()) )
			return;

		TopicItem ai = (TopicItem) AbstractItem.fromPacket(pp);
		items.put(ai.getTopic(), ai);
	}

}
