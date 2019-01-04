package ru.dz.mqtt_udp;

import java.io.IOException;
import java.net.DatagramSocket;

import ru.dz.mqtt_udp.io.SingleSendSocket;
import ru.dz.mqtt_udp.util.GenericPacket;


/** 
 * Server loop to listen to network traffic and process incoming packets.
 * @author dz
 *
 */
public abstract class SubServer 
{

	private DatagramSocket ss = SingleSendSocket.get();

	
	// ------------------------------------------------------------
	// Replies on/off
	// ------------------------------------------------------------
	
	private boolean muted = false;
	public boolean isMuted() {		return muted;	}
	public void setMuted(boolean muted) {		this.muted = muted;	}


	// ------------------------------------------------------------
	// Incoming data process thread
	// ------------------------------------------------------------
	
	volatile private boolean run;

	public boolean isRunning() { return run; }


	public void requestStart()
	{
		if(isRunning()) return;
		start();
	}

	public void requestStop() { run = false; }
	

	protected void start() {
		Runnable target = makeLoopRunnable();
		Thread t = new Thread(target, "MQTT UDP Recv");
		t.start();
	}

	
	private void loop() throws IOException, MqttProtocolException {
		DatagramSocket s = GenericPacket.recvSocket();

		run = true;

		while(run)
		{
			IPacket p = GenericPacket.recv(s);
			if(!muted) preprocessPacket(p);			
			processPacket(p);			
		}

		s.close();
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



	// ------------------------------------------------------------
	// Packet processing
	// ------------------------------------------------------------


	/**
	 * Must be overriden in children to process packet 
	 * @param p
	 * @throws IOException
	 */
	protected abstract void processPacket(IPacket p) throws IOException;


	/** 
	 * Does internal protocol defined packet processing.
	 * @throws IOException 
	 */
	private void preprocessPacket(IPacket p) throws IOException {

		//if (p instanceof PublishPacket) {		} else 
		if( p instanceof PingReqPacket)
		{
			// Reply to ping
			PingRespPacket presp = new PingRespPacket(null);
			presp.send(ss, ((PingReqPacket) p).getFrom().getInetAddress());
		}
		//else if( p instanceof PingRespPacket) {		}

	}

}
