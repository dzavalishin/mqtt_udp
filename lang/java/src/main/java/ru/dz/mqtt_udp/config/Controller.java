package ru.dz.mqtt_udp.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PacketSourceMultiServer;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.TopicFilter;
import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.util.LoopRunner;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

/**
 * 
 * <p>Passive remote configuration controller.</p>
 * 
 * <p>
 * Basic logic is:
 * 
 * Poll for all configurable nodes by sending subscribe for <b>$SYS/conf/#</b>
 * 
 * Get PUBLISH messages with all configurable parameters.
 * 
 * User code must build UI for setting up parameters.
 * 
 * On parameter change we send it updated, request and check if it is set correct.
 * </p>
 * 
 * @author dz
 * 
 * @see lang/c/mqtt_udp_rconfig.c - C implementation of client side for this class.
 *
 */

public class Controller implements Consumer<IPacket> {

	private final static String SYS_CONF_WILD = mqtt_udp_defs.SYS_CONF_PREFIX+"/#";
	
	private LoopRunner lr = new LoopRunner("") {
		
		@Override
		protected void step() throws IOException, MqttProtocolException {
			new SubscribePacket(SYS_CONF_WILD).send();
			sleep(30L*1000L);
			//sleep(2L*1000L);
		}
		
		@Override
		protected void onStop() throws IOException, MqttProtocolException {
			// Empty			
		}
		
		@Override
		protected void onStart() throws IOException, MqttProtocolException {
			// Empty			
		}
	}; 
	
	private TopicFilter rf = new TopicFilter(SYS_CONF_WILD);

	//private IPacketMultiSource ms;
	
	/**
	 * Construct.
	 * @param ms MQTT/UDP network listener which is able to serve multiple consumers.
	 */
	public Controller(IPacketMultiSource ms) 
	{
		//this.ms = ms;
		ms.addPacketSink(this);	
		lr.requestStart();
	}

	public void requestStart()
	{
		lr.requestStart();
	}
	
	@Override
	public void accept(IPacket t) {
		
		if(! (t instanceof PublishPacket))
			return;

		PublishPacket pp = (PublishPacket) t;
			
	
		String topic = pp.getTopic();
		String value = pp.getValueString();

		if( !rf.test(topic) )
			return;
		
		//System.out.println("Got confable "+topic+" = "+value);

		String suffix = topic.substring(mqtt_udp_defs.SYS_CONF_PREFIX.length());
		
		if(suffix.charAt(0) == '/')
			suffix = suffix.substring(1);
		
		//System.out.println("suffix "+suffix);
		
		String[] parts = suffix.split("/");
		
		if( parts.length < 3)
		{
			System.out.println("suffix has < 3 parts");
		}
		
		String host = parts[0];
		String kind = parts[1];
		String name = parts[2];
		
		ConfigurableHost ch = addHost(host, pp.getFrom());
		//System.out.println("host "+host+" kind "+kind+" name "+ name);
		//System.out.println("kind "+kind+" name "+ name);
		
		ConfigurableParameter cp = new ConfigurableParameter(ch, kind, name, value);
		//System.out.println("param "+cp);
		addParameter( cp );
	}
	

	
	
	private Consumer<ConfigurableHost> newHostListener;
	
	private Map<String,ConfigurableHost> hosts = new HashMap<String,ConfigurableHost>();
	
	private ConfigurableHost addHost(String host, IPacketAddress src) {
		
		ConfigurableHost ch = new ConfigurableHost(host,src);
		
		ConfigurableHost old = hosts.get(host);
		
		if( ch.isSameAs(old) )
			return old;
		
		hosts.put(host, ch);
		
		boolean newone = old == null;
		if(newone)
		{
			System.out.println("new host "+ch);
			if(newHostListener != null) newHostListener.accept(ch);
		}
		else
			System.out.println("update host "+old+" to "+ch); // TODO update

		return ch;
	}

	
	
	private Consumer<ConfigurableParameter> newParameterListener;
	
	private Set<ConfigurableParameter> parameters = new HashSet<ConfigurableParameter>();

	private void addParameter(ConfigurableParameter cp) {

		boolean isNew = parameters.add(cp);
		//if( isNew ) System.out.println("new param "+cp);
		//else System.out.println("again "+cp);
		
		if( isNew ) // TODO call for all 
			if( newParameterListener != null )
				newParameterListener.accept( cp );
	}
	
	
	public void setNewHostListener( Consumer<ConfigurableHost> sink ) {
		this.newHostListener = sink;		
	}
	
	public void setNewParameterListener(Consumer<ConfigurableParameter> sink) {
		this.newParameterListener = sink;		
	}
	
	public static void main(String[] args) {
		PacketSourceMultiServer ms = new PacketSourceMultiServer();
		Controller rc = new Controller(ms);
		
		ms.requestStart();
		rc.requestStart();
	}

}
