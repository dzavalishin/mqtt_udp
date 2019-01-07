package ru.dz.mqtt.viewer;

import java.io.IOException;
import java.net.InetAddress;
import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.PingReqPacket;
import ru.dz.mqtt_udp.PingRespPacket;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.MqttUdpRuntimeException;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

// TODO rename to PacketItem, make subclasses per type

/**
 * Container to keep packet data for display and edit. 
 * TODO Actually must be converted to class hierarchy according to packet type.
 * @author dz
 *
 */
public class TopicItem {

	private int packetType = -1;
	
	private String topic;
	private String value;
	private String from = "?";
	private String time = getCurrentTime();

	public TopicItem(int packetType) {
		this.packetType = packetType;
		assertHasNoTopic();
	}
	
	public TopicItem(int packetType, String topic) {
		this.packetType = packetType;
		this.topic = topic;
		this.value = "";
		
		assertHasTopic();
	}


	public TopicItem(int packetType, String topic, String value) {
		this.packetType = packetType;
		this.topic = topic;
		this.value = value;

		assertHasTopic();
	}

	public TopicItem(TopicItem src) {
		this.packetType = src.packetType;
		this.topic = src.topic;
		this.value = src.value;
		this.from  = src.from;
		this.time = src.time;
	}

	
	
	// ---------------------------------------------------
	// C'tors help
	
	public void assertHasTopic() {		assert typeWithTopic();	}
	public void assertHasNoTopic() {		assert !typeWithTopic();	}

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
	
	
	
	
	// ---------------------------------------------------
	// Get/set
	

	@Override
	public String toString() {
		if( packetType == mqtt_udp_defs.PTYPE_PUBLISH)
			return time+":  "+topic+"="+value;
		else if(typeWithTopic())
			return time+":  "+IPacket.getPacketTypeName(packetType)+" \ttopic="+topic;
		else
			return time+":  "+IPacket.getPacketTypeName(packetType);
	}

	public String getTopic() {		return topic;	}

	public void setValue(String value) { this.value = value; }
	public String getValue() {		return value;	}

	public void setFrom(String from) { this.from = from; }
	public String getFrom() {		return from;	}

	public String getTime() {		return time;	}

	
	// ---------------------------------------------------
	
	
	
	// TODO assign value and time only? check for host/topic be same?
	/** Assign all data from src */
	public void assignFrom(TopicItem src) {
		this.topic	= src.topic;
		this.value	= src.value;
		this.from	= src.from;
		this.time	= src.time;		
	}

	public boolean sameTopic( TopicItem t )
	{
		return getTopic().equals(t.getTopic());
	}

	public boolean sameHostAndTopic( TopicItem t )
	{
		return getTopic().equals(t.getTopic()) && getFrom().equals(t.getFrom());
	}

	//public boolean hasTopic() {		return typeWithTopic();	}

	// ---------------------------------------------------
	
	
	public GenericPacket toPacket()
	{
		switch(packetType)
		{
		case mqtt_udp_defs.PTYPE_PUBLISH: return new PublishPacket(topic, value);
		//case mqtt_udp_defs.PTYPE_SUBSCRIBE: return new SubscribePacket(topic, value);
		case mqtt_udp_defs.PTYPE_PINGREQ: return new PingReqPacket();
		case mqtt_udp_defs.PTYPE_PINGRESP: return new PingRespPacket();
		default: break;
		}
		
		// TODO not runtime exception?
		throw new MqttUdpRuntimeException("Unknown pkt type 0x"+Integer.toHexString(packetType));
	}

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
