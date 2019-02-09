package ru.dz.mqtt_udp.util;

import java.io.IOException;

import ru.dz.mqtt_udp.PublishPacket;

public class Pub {

	public static void main(String[] args) throws IOException {
		if(args.length != 2)
		{
			System.err.println("usage: Pub topic message");
			//System.exit(1);
			return;
		}

		String topic = args[0];
		String msg = args[1];

		System.out.println("Will send "+msg+" to "+topic);
				
		PublishPacket pp = new PublishPacket(topic,msg);
		pp.send();
		System.out.println("Sent ok");
	}

}
