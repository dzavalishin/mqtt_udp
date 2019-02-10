package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.SocketException;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubServer;

/**
 * 
 * NB!! ------------- PART OF GLOBAL REGRESS TEST, do not change -----------------------
 * 
 * 
 * @author dz
 *
 */
public class Wait extends SubServer 
{

	private String topic;
	private String value;

	public static void main(String[] args) throws SocketException, IOException, MqttProtocolException 
	{
		Thread timer = new Thread(new Runnable() {		
			@Override
			public void run() {
				sleep(4000);
				System.out.println("Timed out");
				System.exit(-1);				
			}
		});
		timer.start();
		
		if(args.length < 2)
		{
			System.out.println("Will wait for given topic==value, part of global regress test");
			System.exit(2);
		}
		
		Wait srv = new Wait(
				args[0], 
				args[1]);
		srv.start();
	}

	public Wait(String topic, String value) {
		this.topic = topic;
		this.value = value;
		System.out.println("Will wait for "+topic+" = "+value);
	}

	@Override
	protected void processPacket(IPacket p) {
		//System.out.println(p);

		if (p instanceof PublishPacket) 
		{
			PublishPacket pp = (PublishPacket) p;

			if( pp.getTopic().equals(topic) && pp.getValueString().equals(value) )
			{
				System.out.println("Got it!");
				System.exit(0);
			}
		}
	}
}

