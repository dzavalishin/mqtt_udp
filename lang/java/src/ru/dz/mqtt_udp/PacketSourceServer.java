package ru.dz.mqtt_udp;

import java.io.IOException;
import java.util.function.Consumer;

public class PacketSourceServer extends SubServer implements IPacketSource {

	private Consumer<IPacket> sink;

	public PacketSourceServer() {
		start();
	}
	
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

	
	
	@Override
	public void setSink(Consumer<IPacket> sink) {
		this.sink = sink;
	}

	@Override
	protected void processPacket(IPacket p) throws IOException {
		sink.accept(p);
	}

}
