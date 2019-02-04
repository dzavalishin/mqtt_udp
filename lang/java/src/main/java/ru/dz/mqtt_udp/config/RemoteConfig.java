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
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.util.LoopRunner;

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
	
	
	public static void main(String[] args) {
		Set<ConfigurableParameter> itemList = new HashSet<ConfigurableParameter>(); 
		
		//itemList.add(new ConfigurableParameter(host, "topic", "test1", "Trigger"));
		
		PacketSourceMultiServer ms = new PacketSourceMultiServer();
		RemoteConfig rc = new RemoteConfig(ms, "0200AAAAAAAA", itemList);
		
		ms.requestStart();
		rc.requestStart();
	}


	@Override
	public void accept(IPacket t) {
		// TODO Auto-generated method stub
		
	}
}
