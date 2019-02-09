package ru.dz.mqtt_udp.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.items.TopicItem;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

/**
 * <p>Remote configuration data provider</p>
 * 
 * <p>See also Requester class</p>
 * 
 * <p>
 * Will reply to requests (subscribe packets) for given topics
 * sending back publish packets with data.
 * </p>
 *  
 * @author dz
 *
 */

public class Provider implements Consumer<IPacket> {

	//private SubServer ss; // no, need one that can serve multiple listeners with thread pool
	//private ArrayList topics = new ArrayList<>();
	private Map<String,TopicItem> items = new HashMap<>();
	
	public Provider(IPacketMultiSource ms) 
	{
		ms.addPacketSink(this);	
	}

	public void addTopic(String topicName, String topicValue)
	{
		// TODO need class PublishTopicItem?
		items.put(topicName, new TopicItem(mqtt_udp_defs.PTYPE_PUBLISH, topicName, topicValue));
		
	}

	@Override
	public void accept(IPacket t) {
		//System.out.println("Got packet "+t);

		if( !(t instanceof SubscribePacket) ) 
			return;

		SubscribePacket sp = (SubscribePacket) t;

		if( !items.containsKey(sp.getTopic()) )
			return;

		//System.out.println("PROVIDER: Got request for "+sp.getTopic());
		
		TopicItem it = items.get(sp.getTopic());
		
		PublishPacket pp = new PublishPacket(it.getTopic(), it.getValue());
		try {
			pp.send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
