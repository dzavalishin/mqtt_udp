package ru.dz.mqtt_udp;

public class MqttProtocolException extends Exception {
	
	public MqttProtocolException(String message) {
		super(message);
	}
	
	public MqttProtocolException(String message, Throwable cause) {
		super(message,cause);
	}
}
