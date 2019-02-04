package ru.dz.mqtt_udp.util;

/**
 * Types of errors for error handler callback.
 * @author dz
 *
 */
public enum ErrorType {
	IO, 
	Timeout, 
	Protocol, 
	Unexpected, 
	Invalid			// Invalid parameter
}
