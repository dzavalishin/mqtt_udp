package ru.dz.mqtt.viewer;

import java.io.IOException;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubServer;

public class MqttUdpDataSource extends SubServer implements IDataSource {

	private Consumer<TopicItem> sink;

	public MqttUdpDataSource() {
		Runnable target = new Runnable() {
			@Override
			public void run() {
				try {
					loop();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		};
		
		Thread t = new Thread(target, "MQTT UDP Recv");
		t.start();
	}
	
	@Override
	public void setSink(Consumer<TopicItem> sink) {
		this.sink = sink;
	}

	@Override
	protected void processPacket(IPacket p) {
		
		if (p instanceof PublishPacket) {
			
			//System.out.println("Pub pkt "+p);
			
			PublishPacket pp = (PublishPacket) p;			
			sink.accept(new TopicItem(pp.getTopic(), pp.getValueString()));
		}
		else
			System.out.println(p);
		
	}

}
