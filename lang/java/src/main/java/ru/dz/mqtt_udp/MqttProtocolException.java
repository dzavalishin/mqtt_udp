package ru.dz.mqtt_udp;

public class MqttProtocolException extends Exception {
	
	private static final long serialVersionUID = 9117724925722139158L;

	public MqttProtocolException(String message) {
		super(message);
	}
	
	public MqttProtocolException(String message, Throwable cause) {
		super(message,cause);
	}
}
