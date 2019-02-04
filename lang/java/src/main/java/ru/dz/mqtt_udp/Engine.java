package ru.dz.mqtt_udp;

import ru.dz.mqtt_udp.util.Throttle;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public class Engine {

	private static boolean signatureRequired = false;
	private static String signatureKey = "signPassword";

	
	public static boolean isSignatureRequired() {		return signatureRequired;	}
	public static void setSignatureRequired(boolean req) {		signatureRequired = req;	}
	
	
	public static String getSignatureKey() {		return signatureKey;	}
	public static void setSignatureKey(String key) {		signatureKey = key;	}

	
	private static Throttle t = new Throttle();


	/**
	 * Set packet send rate.
	 * @param msec average time in milliseconds between packets. Set to 0 to turn throttling off.
	 */
	public static void setThrottle(int msec) {		t.setThrottle(msec);	}
	
	/**
	 * Must be called in packet send code.
	 * Will put caller asleep to make sure packets are sent in a right pace. 
	 */
	public static void throttle() {				t.throttle();			}

	public static String getVersionString() {
		return String.format("%d.%d", mqtt_udp_defs.PACKAGE_VERSION_MAJOR, mqtt_udp_defs.PACKAGE_VERSION_MINOR);
	}

}
