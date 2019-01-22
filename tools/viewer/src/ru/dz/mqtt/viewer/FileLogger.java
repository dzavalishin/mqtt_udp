package ru.dz.mqtt.viewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ru.dz.mqtt_udp.items.AbstractItem;

public class FileLogger {
	private BufferedWriter writer = null;

	public void startLog( File fname ) throws IOException
	{
		FileWriter fw = new FileWriter(fname);
		writer = new BufferedWriter(fw);
	}
	
	public void stopLog()
	{
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer = null;
	}
	
	public void logItem(AbstractItem ti)
	{
		String s = ti.toString();
		try {			
			if(writer != null) 
				{
				//System.out.println(s);
				writer.write(s+"\n");
				writer.flush();
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
