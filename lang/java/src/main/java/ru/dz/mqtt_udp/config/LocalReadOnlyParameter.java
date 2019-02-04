package ru.dz.mqtt_udp.config;

import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;

public class LocalReadOnlyParameter extends LocalConfigurableParameter {

	public LocalReadOnlyParameter(String kind, String name, String value) {
		super(kind, name, value);
	}

	@Override
	public void setValue(String v) { /** Ignore */ 
		GlobalErrorHandler.handleError(ErrorType.Invalid, "sentValue is forbidden for LocalReadOnlyParameter");
	}
	
	@Override
	public void sendNewValue(String v) {
		GlobalErrorHandler.handleError(ErrorType.Invalid, "sendNewValue is forbidden for LocalReadOnlyParameter");
	}
	
}
