package ru.dz.mqtt_udp;

import java.io.IOException;
import java.net.DatagramSocket;

import ru.dz.mqtt_udp.io.SingleSendSocket;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.LoopRunner;


/** 
 * Server loop to listen to network traffic and process incoming packets.
 * @author dz
 *
 */
public abstract class SubServer extends LoopRunner 
{

	private DatagramSocket ss = SingleSendSocket.get();
	private DatagramSocket s;

	public SubServer() {
		super("MQTT UDP Recv");
	}	

	@Override
	protected void onStart() throws IOException, MqttProtocolException {
		s = GenericPacket.recvSocket();
	}

	@Override
	protected void step() throws IOException, MqttProtocolException 
	{
		IPacket p = GenericPacket.recv(s);
		if(!muted) preprocessPacket(p);			
		processPacket(p);			
	}

	@Override
	protected void onStop() throws IOException, MqttProtocolException {
		s.close();
	}

	// ------------------------------------------------------------
	// Replies on/off
	// ------------------------------------------------------------

	private boolean muted = false;
	public boolean isMuted() {		return muted;	}
	/** 
	 * Set muted mode. In muted mode server loop won't respond to any incoming packets
	 * (such as PINGREQ) automatically.
	 * 
	 * @param muted If true - mute replies.
	 */
	public void setMuted(boolean muted) {		this.muted = muted;	}


	// ------------------------------------------------------------
	// Incoming data process thread
	// ------------------------------------------------------------

	//volatile private boolean run = false;

	//public boolean isRunning() { return run; }
	//public boolean isRunning() { return lr.isRunning(); }
	//public void requestStart() { lr.requestStart(); }
	//public void requestStop() { lr.requestStop(); }

	/**
	 * Request to start reception loop thread.
	 * /
	public void requestStart()
	{
		if(isRunning()) return;
		start();
	}

	/**
	 * Request to stop reception loop thread.
	 * /
	public void requestStop() { run = false; }

	/**
	 * Worker: start reception loop thread.
	 * /
	protected void start() {
		Runnable target = makeLoopRunnable();
		Thread t = new Thread(target, "MQTT UDP Recv");
		t.start();
	}* /


	private void loop() throws IOException, MqttProtocolException {
		DatagramSocket s = GenericPacket.recvSocket();

		run = true;

		while(run)
		{
			//System.out.print("Listen loop run");
			IPacket p = GenericPacket.recv(s);
			if(!muted) preprocessPacket(p);			
			processPacket(p);			
		}

		s.close();
	} * /


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
	} */



	// ------------------------------------------------------------
	// Packet processing
	// ------------------------------------------------------------


	/**
	 * Must be overridden in children to process packet 
	 * @param p packet to process
	 * @throws IOException if IO error
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
			PingRespPacket presp = new PingRespPacket();
			//presp.send(ss, ((PingReqPacket) p).getFrom().getInetAddress());
			// decided to broadcast ping replies
			presp.send(ss);
		}
		else if( p instanceof PublishPacket) {
			PublishPacket pp = (PublishPacket) p;
			
			int qos = pp.getQoS();
			if( qos != 0 )
			{
				System.out.println("QoS");
				int maxQos = Engine.getMaxReplyQoS();
				qos = Integer.min(qos, maxQos);
				new PubAckPacket(pp, qos).send();
			}
		}

	}

}
