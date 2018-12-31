package ru.dz.mqtt.viewer;

import java.io.IOException;

import ru.dz.mqtt_udp.PingReqPacket;

public class PingSender {

	volatile private boolean run = true;

	public PingSender() {
		Runnable target = makeLoopRunnable();
		Thread t = new Thread(target, "MQTT UDP Ping");
		t.start();
	}
	
	public void setEnabled(boolean en) { run = en; }
	public boolean isEnabled() { return run; }
	
	
	
	private Runnable makeLoopRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					loop();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}/* catch (MqttProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/				
			}
		};
	}

	protected void loop() throws IOException {
		try {
			synchronized (this) {				
				this.wait(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(run)
			sendPing();
	}


	private static final byte[] empty = new byte[0];
	private void sendPing() throws IOException {
		PingReqPacket ping = new PingReqPacket(empty, (byte) 0, null );
		ping.send();
	}

	
	
}
