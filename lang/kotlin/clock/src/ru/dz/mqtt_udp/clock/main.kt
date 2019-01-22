package ru.dz.mqtt_udp.clock

import ru.dz.mqtt_udp.PublishPacket
import java.text.SimpleDateFormat
import java.util.Date

val TOPIC = "\$SYS/clock";

/**
 * Main.
 */
fun main(args : Array<String>)
{
	println("MQTT/UDP Clock");
	//println("MQTT/UDP Clock: publish \$SYS/time once a minute");
	
	while(true)
	{
		//val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
		val sdf = SimpleDateFormat("hh:mm:ss")
		val currentDate = sdf.format(Date())
		
		//System.out.println(" C DATE is  "+currentDate)		
		val pp = PublishPacket(TOPIC, currentDate);
		pp.send();
		Thread.sleep(1_000*60);
	}
	
}