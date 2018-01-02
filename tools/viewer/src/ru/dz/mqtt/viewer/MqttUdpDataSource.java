package ru.dz.mqtt.viewer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PingReqPacket;
import ru.dz.mqtt_udp.PingRespPacket;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubServer;
import ru.dz.mqtt_udp.util.GenericPacket;

public class MqttUdpDataSource extends SubServer implements IDataSource {

	private Consumer<TopicItem> sink;
	private DatagramSocket ss;

	public MqttUdpDataSource() throws SocketException 
	{
		ss = GenericPacket.sendSocket();
		
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

	/** 
	 * Called from SubServer code
	 * @throws IOException 
	 */
	@Override
	protected void processPacket(IPacket p) throws IOException {

		if (p instanceof PublishPacket) {

			//System.out.println("Pub pkt "+p);

			PublishPacket pp = (PublishPacket) p;			
			TopicItem ti = new TopicItem(pp.getTopic(), pp.getValueString());
			ti.setFrom(pp.getFrom().toString());
			sink.accept(ti);
		}
		else if( p instanceof PingReqPacket)
		{
			PingRespPacket presp = new PingRespPacket();
			presp.send(ss, ((PingReqPacket) p).getFrom().getInetAddress());
			System.out.println(p);
		}
		else
			System.out.println(p);

	}

}
