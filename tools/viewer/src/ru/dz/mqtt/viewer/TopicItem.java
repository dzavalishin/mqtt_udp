package ru.dz.mqtt.viewer;

public class TopicItem {

	private String topic;
	private String value;

	public TopicItem(String topic) {
		this.topic = topic;
		this.value = "";
	}
	
	public TopicItem(String topic, String value) {
		this.topic = topic;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return topic+"="+value;
	}
}
