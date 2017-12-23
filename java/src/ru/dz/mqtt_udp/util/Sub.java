package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.SocketException;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PublishPacket;

public class Sub {

	public static void main(String[] args) throws SocketException, IOException, MqttProtocolException {
		IPacket p = GenericPacket.recv();
		System.out.println(p);
		
		if (p instanceof PublishPacket) {
			PublishPacket pp = (PublishPacket) p;
			
			// now use pp.getTopic() and pp.getValueString() or pp.getValueRaw()
		}
	}

}
