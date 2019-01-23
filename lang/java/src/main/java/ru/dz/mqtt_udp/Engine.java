package ru.dz.mqtt_udp;

import ru.dz.mqtt_udp.util.Throttle;

public class Engine {

	private static boolean signatureRequired = false;
	private static String signatureKey = "signPassword";

	
	public static boolean isSignatureRequired() {		return signatureRequired;	}
	public static void setSignatureRequired(boolean req) {		signatureRequired = req;	}
	
	
	public static String getSignatureKey() {		return signatureKey;	}
	public static void setSignatureKey(String key) {		signatureKey = key;	}

	
	static Throttle t = new Throttle();


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

}
