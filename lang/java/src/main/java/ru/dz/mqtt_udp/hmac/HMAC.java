package ru.dz.mqtt_udp.hmac;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;
import ru.dz.mqtt_udp.util.MqttUdpRuntimeException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class HMAC {



	public static String hmacDigestMD5(String msg, String keyString) {
		return hmacDigest(msg, keyString, "HmacMD5");
	}

	public static String hmacDigestSHA256(String msg, String keyString) {
		return hmacDigest(msg, keyString, "HmacSHA256");
	}



	
	public static byte[] hmacDigestMD5(byte[] msg, String keyString) {
		return hmacDigest(msg, keyString, "HmacMD5");
	}
	
	public static byte[] hmacDigestSHA256(byte[] msg, String keyString) {
		return hmacDigest(msg, keyString, "HmacSHA256");
	}
	
	
	
	public static String hmacDigest(String msg, String keyString, String algo) 
	{
		
		try {
			return makeHexString( hmacDigest(msg.getBytes("ASCII"), keyString, algo) );
		} catch (UnsupportedEncodingException e) {
			GlobalErrorHandler.handleError(ErrorType.Unexpected, e);
			return null;
		}
		
	}



	public static byte[] hmacDigest(byte[] msg, String keyString, String algo) {
		try {

			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(key);

			return mac.doFinal(msg);

		} catch (Throwable e) {
			//GlobalErrorHandler.handleError(ErrorType.Unexpected, e);
			throw new MqttUdpRuntimeException(e);
		}
	}


	public static String makeHexString(byte[] bytes) {
		String digest;
		StringBuffer hash = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hash.append('0');
			}
			hash.append(hex);
		}
		digest = hash.toString();
		return digest;
	}








}