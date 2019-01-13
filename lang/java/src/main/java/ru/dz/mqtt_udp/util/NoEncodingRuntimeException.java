package ru.dz.mqtt_udp.util;

public class NoEncodingRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -1920650021506857876L;

	public NoEncodingRuntimeException() {
	}

	public NoEncodingRuntimeException(String message) {
		super(message);
	}

	public NoEncodingRuntimeException(Throwable cause) {
		super(cause);
	}

	public NoEncodingRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoEncodingRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
