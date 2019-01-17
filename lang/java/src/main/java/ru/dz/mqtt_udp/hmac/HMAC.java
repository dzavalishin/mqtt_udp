package ru.dz.mqtt_udp.hmac;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class HMAC {

	public static void main(String[] args) throws Exception {
		System.out.println(hmacDigest("text", "key", "HmacSHA256"));
	}


	public static String hmacDigestMD5(String msg, String keyString) {
		return hmacDigest(msg, keyString, "HmacMD5");
	}

	public static String hmacDigestSHA256(String msg, String keyString) {
		return hmacDigest(msg, keyString, "HmacSHA256");
	}


	public static String hmacDigest(String msg, String keyString, String algo) {
		String digest = null;
		try {
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(key);

			byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

			StringBuffer hash = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();
		} catch (Throwable e) {
			GlobalErrorHandler.handleError(ErrorType.Unexpected, e);
		}
		return digest;
	}
}