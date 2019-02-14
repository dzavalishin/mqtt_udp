package ru.dz.mqtt_udp.util;

import java.io.IOException;

import ru.dz.mqtt_udp.Engine;
import ru.dz.mqtt_udp.PublishPacket;
/**
 * NB!
 * 
 * This program is used in global regress test and its output is checked.
 * See test/runner
 * 
 * @author dz
 *
 */
public class Pub {

	public static void main(String[] args) throws IOException {
		String topic = null;
		String msg = null;

		if(args.length == 4)
		{
			if( !args[0].equals("-s") )
			{
				usage();
				return;
			}
			Engine.setSignatureKey(args[1]);
			topic = args[2];
			msg = args[3];
		}
		else
		{

			if(args.length != 2)
			{
				usage();
				return;
			}

			topic = args[0];
			msg = args[1];
		}

		System.out.println("Will send "+msg+" to "+topic);

		PublishPacket pp = new PublishPacket(topic,msg);
		pp.send();
		System.out.println("Sent ok");
	}

	public static void usage() {
		System.err.println("usage: Pub [-s SignaturePassword] topic message");
	}

}
