package ru.dz.mqtt_udp.proto;


import java.util.Arrays;

import ru.dz.mqtt_udp.hmac.HMAC;

public class TTR_Signature extends TaggedTailRecord {
	private final static byte myTag = (byte)'s'; 
	public final static int SIGLEN = 16+1+1; // len of signature TTRecord

	//private static Charset cs;
	
	/*static {
		cs = Charset.forName("US-ASCII");
	}*/

	private byte[] sig;
	
	public TTR_Signature(byte tag, byte[] rec, int rawLength) {
		super( tag, rawLength );
		//sig = new String( rec, cs );
		sig = rec;
	}

	public TTR_Signature(byte[] signature) {
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
		byte[] our = HMAC.hmacDigestMD5( data, keyString );
		//ByteArray.dumpBytes("our", our );
		//ByteArray.dumpBytes("his", sig );
		//return sig.equals( our );
		return Arrays.equals(sig, our);
	}

	@Override
	public byte[] toBytes() {
		//try {
		return toBytes( tag, sig );
		//return toBytes( tag, sig.getBytes("ASCII"));
		/*} catch (UnsupportedEncodingException e) {
			GlobalErrorHandler.handleError(ErrorType.Unexpected, e);
			return null;
		}*/
	}

	public byte[] getSignature() {
		return sig;
	}

	
	@Override
	public String toString() {
		//return String.format("TTR Signature %[B", sig);
		return "TTR Signature";
	}

	
}
