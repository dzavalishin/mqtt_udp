package ru.dz.mqtt_udp.config;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PacketSourceMultiServer;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.TopicFilter;
import ru.dz.mqtt_udp.util.LoopRunner;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

/**
 * 
 * <p>Passive remote configuration controllable node.</p>
 * 
 * <p>
 * This class serves configuration items to be remotely configurable.
 * </p>
 * 
 * @see Controller
 * @see lang/c/mqtt_udp_rconfig.c - C implementation of same functionality.
 * @see <a href="https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-message-content-specification">Wiki</a>
 * 
 * @author dz
 *
 */
public class RemoteConfig implements Consumer<IPacket> {
	private LoopRunner lr = new LoopRunner("Remote Config Controllable") 
	{
		@Override
		protected void step() throws IOException, MqttProtocolException {
			//new SubscribePacket(SYS_CONF_WILD).send();
			sleep(30L*1000L);
			//sleep(2L*1000L);
		}
		
		@Override
		protected void onStop() throws IOException, MqttProtocolException { /** empty */ }		
		@Override
		protected void onStart() throws IOException, MqttProtocolException { /** empty */ }
	};
	private String macAddress;
	private Collection<ConfigurableParameter> items; 

	
	public RemoteConfig( IPacketMultiSource ms, String macAddress, Collection<ConfigurableParameter> items ) {
		this.macAddress = macAddress;
		this.items = items;
		ms.addPacketSink(this);	
	}

	public void requestStart()
	{
		lr.requestStart();
	}
	
	

	private final static String SYS_CONF_WILD = mqtt_udp_defs.SYS_CONF_PREFIX+"/#";
	private TopicFilter rf = new TopicFilter(SYS_CONF_WILD);

	/**
	 *  Incoming packet
	 * 
	 */
	@Override
	public void accept(IPacket p) 
	{
		if (p instanceof SubscribePacket) {
			SubscribePacket sp = (SubscribePacket) p;
			
			if( rf.test(sp.getTopic()) )
			{
				sendAllConfigurableTopics();
				return;
			}
			
			// possible request for some specific one
			sendConfigurableTopic(sp.getTopic());
			
		}
		
		if (p instanceof PublishPacket) {
			PublishPacket pp = (PublishPacket) p;
			
			setLocalValue( pp );
		}		
	}

	private void setLocalValue(PublishPacket pp) {
		items.forEach( item -> {
			if( item.topicIs(pp.getTopic()) )
				item.setValue(pp.getValueString());
		} );
	}

	private void sendConfigurableTopic(String topic) {
		items.forEach( item -> {
			if( item.topicIs(topic))
				item.sendCurrectValue();
		} );
	}

	private void sendAllConfigurableTopics() 
	{
		items.forEach( item -> item.sendCurrectValue() );		
	}








	public static void main(String[] args) {
		Set<ConfigurableParameter> itemList = new HashSet<ConfigurableParameter>(); 

		String mac = ConfigurableHost.getMachineMacAddressString();
		ConfigurableHost ch = new ConfigurableHost(mac, null ); 
		
		itemList.add(new ConfigurableParameter(ch, "topic", "test1", "Trigger"));
		
		PacketSourceMultiServer ms = new PacketSourceMultiServer();
		RemoteConfig rc = new RemoteConfig(ms, mac, itemList);
		
		ms.requestStart();
		rc.requestStart();
	}




}
