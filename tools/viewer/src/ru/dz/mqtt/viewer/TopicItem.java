package ru.dz.mqtt.viewer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TopicItem {

	private String topic;
	private String value;
	private String from = "?";
	private final String time = getCurrentTime();

	public TopicItem(String topic) {
		this.topic = topic;
		this.value = "";
	}

	static private final SimpleDateFormat ft3 = new SimpleDateFormat("hh:mm:ss");
	private static String getCurrentTime()
	{
		Date dNow = new Date( );
		return ft3.format(dNow);
	}

	public TopicItem(String topic, String value) {
		this.topic = topic;
		this.value = value;
	}

	@Override
	public String toString() {
		return time+":  "+topic+"="+value;
	}

	public String getTopic() {		return topic;	}

	public void setValue(String value) { this.value = value; }
	public String getValue() {		return value;	}

	public void setFrom(String from) { this.from = from; }
	public String getFrom() {		return from;	}

	public String getTime() {		return time;	}

}
