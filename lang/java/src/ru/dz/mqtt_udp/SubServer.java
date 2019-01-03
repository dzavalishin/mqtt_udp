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

	volatile private boolean run;
	private DatagramSocket ss = SingleSendSocket.get();

	public void loop() throws IOException, MqttProtocolException {
		DatagramSocket s = GenericPacket.recvSocket();
	
		run = true;
		
		while(run)
		{
			IPacket p = GenericPacket.recv(s);
			preprocessPacket(p);			
			processPacket(p);			
		}
		
		s.close();
	}

	public void requestStop() { run = false; }
	
	public boolean isRunning() { return run; }
	
	protected abstract void processPacket(IPacket p) throws IOException;

	
	/** 
	 * Called from SubServer code, does internal protocol
	 * defined packet processing.
	 * @throws IOException 
	 */
	protected void preprocessPacket(IPacket p) throws IOException {

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
