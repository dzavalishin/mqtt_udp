package ru.dz.mqtt_udp;

public class Engine {

	private static boolean signatureRequired = false;
	private static String signatureKey = "signPassword";

	
	public static boolean isSignatureRequired() {		return signatureRequired;	}
	public static void setSignatureRequired(boolean req) {		signatureRequired = req;	}
	
	
	public static String getSignatureKey() {		return signatureKey;	}
	public static void setSignatureKey(String key) {		signatureKey = key;	}
	

}
