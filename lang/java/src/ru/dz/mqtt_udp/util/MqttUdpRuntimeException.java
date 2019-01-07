package ru.dz.mqtt_udp.util;

/**
 * To throw in "impossible" cases.
 * @author dz
 *
 */
public class MqttUdpRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -5457668271437431299L;

	public MqttUdpRuntimeException() {
	}

	public MqttUdpRuntimeException(String message) {
		super(message);
	}

	public MqttUdpRuntimeException(Throwable cause) {
		super(cause);
	}

	public MqttUdpRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MqttUdpRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
