package ru.dz.mqtt_udp.config;

import java.util.ArrayList;

import ru.dz.mqtt_udp.SubServer;

/**
 * <p>Remote configuration data provider</p>
 * 
 * <p>Will reply to</p>
 *  
 * @author dz
 *
 */

public class Provider {

	private SubServer ss; // no, need one that can serve multiple listeners with thread pool
	private ArrayList topics = new ArrayList<>();
	
	public Provider(SubServer ss) 
	{
		this.ss = ss;	
	}

	public void addTopic(String topicName, String topicValue)
	{
		
	}
	
}
