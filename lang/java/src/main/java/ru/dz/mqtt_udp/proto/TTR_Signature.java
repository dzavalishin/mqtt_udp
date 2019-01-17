package ru.dz.mqtt_udp.proto;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ru.dz.mqtt_udp.hmac.HMAC;
import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;

public class TTR_Signature extends TaggedTailRecord {
	private final static byte myTag = (byte)'s'; 

	private static Charset cs;
	
	static {
		cs = Charset.forName("US-ASCII");
	}

	private String sig;
	
	public TTR_Signature(byte tag, byte[] rec, int rawLength) {
		super( tag, rawLength );
		sig = new String( rec, cs );
	}

	public TTR_Signature(String signature) {
		super( myTag, -1 );
		sig = signature;
	}
	
	/**
	 * Check if packet has correct signature.
	 * @param data part of packet to check.
	 * @param keyString password.
	 * @return True if signature is correct.
	 */
	public boolean check( byte[] data, String keyString )
	{
		return sig.equalsIgnoreCase( HMAC.hmacDigestSHA256( data, keyString ) );
	}

	@Override
	public byte[] toBytes() {
		try {
			return toBytes( tag, sig.getBytes("ASCII"));
		} catch (UnsupportedEncodingException e) {
			GlobalErrorHandler.handleError(ErrorType.Unexpected, e);
			return null;
		}
	}
	
}
