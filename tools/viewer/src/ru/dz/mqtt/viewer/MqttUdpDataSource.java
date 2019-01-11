package ru.dz.mqtt.viewer;

import java.io.IOException;
import java.net.SocketException;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.PingReqPacket;
import ru.dz.mqtt_udp.PingRespPacket;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubServer;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.items.TopicItem;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;
import ru.dz.mqtt_udp.items.AbstractItem;

public class MqttUdpDataSource extends SubServer implements IDataSource {

	private Consumer<AbstractItem> sink;

	public MqttUdpDataSource() throws SocketException 
	{
		start();
	}

	@Override
	public void setSink(Consumer<AbstractItem> sink) {
		this.sink = sink;
	}

	/** 
	 * Called from SubServer code
	 * @throws IOException 
	 */
	@Override
	protected void processPacket(IPacket p) throws IOException {

		sink.accept( AbstractItem.fromPacket(p) );
		
		/*
		if (p instanceof PublishPacket) {

			//System.out.println("Pub pkt "+p);

			PublishPacket pp = (PublishPacket) p;			
			TopicItem ti = new TopicItem( p.getType(), pp.getTopic(), pp.getValueString() );
			ti.setFrom(pp.getFrom().toString());
			sink.accept(ti);
		} else if( p instanceof SubscribePacket)
		{
			SubscribePacket sp = (SubscribePacket) p;			
			TopicItem ti = new TopicItem( mqtt_udp_defs.PTYPE_SUBSCRIBE, sp.getTopic() );
			ti.setFrom(p.getFrom().toString());
			sink.accept(ti);
		} else if( p instanceof PingReqPacket)
		{
			// Subserver replies itself now
			//PingRespPacket presp = new PingRespPacket(null);
			//presp.send(ss, ((PingReqPacket) p).getFrom().getInetAddress());

			// TODO hack
			//TopicItem ti = new TopicItem(mqtt_udp_defs.PTYPE_PINGREQ, "PingRequest", p.toString());
			TopicItem ti = new TopicItem(mqtt_udp_defs.PTYPE_PINGREQ);
			ti.setFrom(p.getFrom().toString());
			sink.accept(ti);
		} else if( p instanceof PingRespPacket)
		{
			//PingRespPacket pr = (PingRespPacket)p; 
			// TODO hack
			//TopicItem ti = new TopicItem("PingResponce", p.toString());
			TopicItem ti = new TopicItem(mqtt_udp_defs.PTYPE_PINGRESP);
			ti.setFrom(p.getFrom().toString());
			sink.accept(ti);
		}
		else
		{
			System.out.println(p);
			// TODO hack
			TopicItem ti = new TopicItem( 0, "UnknownPacket", p.toString());
			ti.setFrom(p.getFrom().toString());
			sink.accept(ti);
		}
		*/
	}

}
