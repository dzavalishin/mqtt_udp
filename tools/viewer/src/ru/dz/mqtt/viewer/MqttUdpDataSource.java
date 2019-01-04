package ru.dz.mqtt.viewer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PacketSourceServer;
import ru.dz.mqtt_udp.PingReqPacket;
import ru.dz.mqtt_udp.PingRespPacket;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubServer;
import ru.dz.mqtt_udp.io.SingleSendSocket;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public class MqttUdpDataSource extends SubServer implements IDataSource {
//public class MqttUdpDataSource extends PacketSourceServer implements IDataSource {

	private Consumer<TopicItem> sink;
	//private DatagramSocket ss;

	public MqttUdpDataSource() throws SocketException 
	{
		//ss = GenericPacket.sendSocket();
		//ss = SingleSendSocket.get();
		start();
	}

	/* move up
	private void start() {
		Runnable target = makeLoopRunnable();
		Thread t = new Thread(target, "MQTT UDP Recv");
		t.start();
	}

	public void requestStart()
	{
		if(isRunning()) return;
		start();
	}
	
	
	private Runnable makeLoopRunnable() {
		return new Runnable() {
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
	}
*/
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
			TopicItem ti = new TopicItem( p.getType(), pp.getTopic(), pp.getValueString() );
			ti.setFrom(pp.getFrom().toString());
			sink.accept(ti);
		} else 
		if( p instanceof PingReqPacket)
		{
			// Subserver replies itself now
			//PingRespPacket presp = new PingRespPacket(null);
			//presp.send(ss, ((PingReqPacket) p).getFrom().getInetAddress());
			
			// TODO hack
			//TopicItem ti = new TopicItem(mqtt_udp_defs.PTYPE_PINGREQ, "PingRequest", p.toString());
			TopicItem ti = new TopicItem(mqtt_udp_defs.PTYPE_PINGREQ);
			ti.setFrom(p.getFrom().toString());
			sink.accept(ti);
		}else 
		if( p instanceof PingRespPacket)
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

}
