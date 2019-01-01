package ru.dz.mqtt_udp;

import java.io.IOException;
import java.net.DatagramSocket;

import ru.dz.mqtt_udp.util.GenericPacket;

public abstract class SubServer 
{

	volatile private boolean run;

	public void loop() throws IOException, MqttProtocolException {
		DatagramSocket s = GenericPacket.recvSocket();
	
		run = true;
		
		while(run)
		{
			IPacket p = GenericPacket.recv(s);
			processPacket(p);			
		}
		
		s.close();
	}

	public void requestStop() { run = false; }
	
	public boolean isRunning() { return run; }
	
	protected abstract void processPacket(IPacket p) throws IOException;

}
