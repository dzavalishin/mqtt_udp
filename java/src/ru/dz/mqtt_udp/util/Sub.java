package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PublishPacket;

public class Sub {

	public static void main(String[] args) throws SocketException, IOException, MqttProtocolException 
	{
		if( args.length == 1 && args[0].equalsIgnoreCase("-f"))
		{
			loop();
			System.exit(0);
		}
		
		IPacket p = GenericPacket.recv();
		processPacket(p);
	}

	private static void loop() throws IOException, MqttProtocolException {
		DatagramSocket s = GenericPacket.recvSocket();
		
		while(true)
		{
			IPacket p = GenericPacket.recv(s);
			processPacket(p);
		}
	}

	private static void processPacket(IPacket p) {
		System.out.println(p);
		
		if (p instanceof PublishPacket) {
			PublishPacket pp = (PublishPacket) p;
			
			// now use pp.getTopic() and pp.getValueString() or pp.getValueRaw()
		}
		
	}

}
