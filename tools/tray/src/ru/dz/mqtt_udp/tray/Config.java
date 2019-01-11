package ru.dz.mqtt_udp.tray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

	public final String topic1;
	public final String topic2;
	public final String topic1Header;
	public final String topic2Header;

	public final String controlTopic;
	
	public Config() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(new File("mqttudptray.ini")));

		//SOME_INT_VALUE = Integer.valueOf(props.getProperty("SOME_INT_VALUE", "1"));
		//SOME_DOUBLE_VALUE = Double.valueOf(props.getProperty("SOME_DOUBLE_VALUE", "1.0"));
		
		topic1 = props.getProperty("topic1", null);
		topic2 = props.getProperty("topic2", null);

		topic1Header = props.getProperty("topic1header", topic1);
		topic2Header = props.getProperty("topic2header", topic2);

		controlTopic = props.getProperty("controltopic", null);
		
		/*
		// Предположим, что в настройках находится список целых через точку с запятой
		String[] parts = props.getProperty("SOME_INT_ARRAY").split(";");
		SOME_INT_ARRAY = new int[parts.length];
		for (int i = 0; i < parts.length; ++i)
		{
			SOME_INT_ARRAY[i] = Integer.valueOf(parts[i]);
		}
		*/
	}

}
