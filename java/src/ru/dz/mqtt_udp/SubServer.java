package ru.dz.mqtt_udp;

import java.io.IOException;
import java.net.DatagramSocket;

import ru.dz.mqtt_udp.util.GenericPacket;

public abstract class SubServer 
{

	public void loop() throws IOException, MqttProtocolException {
		DatagramSocket s = GenericPacket.recvSocket();
		
		while(true)
		{
			IPacket p = GenericPacket.recv(s);
			processPacket(p);
		}
	}

	protected abstract void processPacket(IPacket p) throws IOException;

}
