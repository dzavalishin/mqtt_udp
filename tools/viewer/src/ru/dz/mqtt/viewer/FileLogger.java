package ru.dz.mqtt.viewer;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
	
	public void logItem(TopicItem ti)
	{
		try {
			if(writer != null) writer.write(ti.toString()+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
