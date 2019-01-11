package ru.dz.mqtt_udp.items;

import java.io.IOException;
import java.net.InetAddress;

import ru.dz.mqtt_udp.PingReqPacket;
import ru.dz.mqtt_udp.PingRespPacket;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.MqttUdpRuntimeException;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

/**
 * <p>Container to keep packet data for display and edit.</p> 
 *
 * @author dz
 *
 */

public abstract class AbstractItem {
	protected int packetType = -1;

	private String from = "?";
	private String time = getCurrentTime();
	
	/*public AbstractItem() {
		// TODO Auto-generated constructor stub
	}*/

	protected AbstractItem(int packetType) {
		this.packetType = packetType;
	}

	protected AbstractItem(AbstractItem src) {
		this.from  = src.from;
		this.time = src.time;
	}

	
	
	
	// ---------------------------------------------------
	// C'tors help
	
	protected void assertHasTopic() {		assert typeWithTopic();	}
	protected void assertHasNoTopic() {		assert !typeWithTopic();	}

	public boolean typeWithTopic() {
		return (packetType == mqtt_udp_defs.PTYPE_PUBLISH) ||
		(packetType == mqtt_udp_defs.PTYPE_PUBACK) ||
		(packetType == mqtt_udp_defs.PTYPE_SUBSCRIBE) ||
		(packetType == mqtt_udp_defs.PTYPE_SUBACK) ||
		(packetType == mqtt_udp_defs.PTYPE_UNSUBSCRIBE) ||
		(packetType == mqtt_udp_defs.PTYPE_UNSUBACK);
	}

	
	
	
	
	
	
	
	
	//static private final SimpleDateFormat ft3 = new SimpleDateFormat("hh:mm:ss");
	private static String getCurrentTime()
	{
		//Date dNow = new Date( );
		//return ft3.format(dNow);
		return java.time.LocalTime.now().toString();
	}
	

	
	
	public void setFrom(String from) { this.from = from; }
	public String getFrom() {		return from;	}

	public String getTime() {		return time;	}
	

	// TODO assign value and time only? check for host/topic be same?
	/** Assign all data from src */
	public void assignFrom(AbstractItem src) {
		this.packetType = src.packetType;
		this.from       = src.from;
		this.time       = src.time;		
	}

	
	
	
	public GenericPacket toPacket()
	{
		switch(packetType)
		{
		case mqtt_udp_defs.PTYPE_PINGREQ: return new PingReqPacket();
		case mqtt_udp_defs.PTYPE_PINGRESP: return new PingRespPacket();
		default: break;
		}
		
		// TODO not runtime exception?
		throw new MqttUdpRuntimeException("Unknown pkt type 0x"+Integer.toHexString(packetType));
	}

	/*
	 * 
	public static AbstractItem fromPacket( IPacket p )
	{
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
		
	}
	
	*/
	
	
	
	
	
	

	public void sendToAll() throws IOException
	{
		GenericPacket pkt = toPacket();
		pkt.send();
	}

	public void sendTo(InetAddress addr) throws IOException
	{
		GenericPacket pkt = toPacket();
		pkt.send( addr );
	}
	
	
	
}
