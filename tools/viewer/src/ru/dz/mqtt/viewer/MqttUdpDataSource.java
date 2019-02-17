package ru.dz.mqtt.viewer;

import java.io.IOException;
import java.net.SocketException;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.SubServer;
import ru.dz.mqtt_udp.items.AbstractItem;

@Deprecated
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
	}

}
