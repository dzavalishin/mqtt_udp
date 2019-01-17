package ru.dz.mqtt_udp.proto;

import java.nio.ByteOrder;

import ru.dz.mqtt_udp.util.MqttUdpRuntimeException;

/**
 * <p>Protocol extension. See https://github.com/dzavalishin/mqtt_udp/wiki/Tagged-Tail</p>
 * 
 * @author dz
 *
 */
public abstract class TaggedTailRecord {



	protected final int rawLength;
	protected final byte tag;


	public int getRawLength() { return rawLength; }
	public byte getTag() { return tag; }

	protected TaggedTailRecord(byte tag, int rawLngth)
	{
		this.tag = tag;
		rawLength = rawLngth;		
	}


	public static TaggedTailRecord fromBytes(  byte[] raw )
	{
		int rawLength = 1; // tag
		byte tag = raw[0];

		int dlen = 0;
		int pos = 1;

		while(true)
		{
			byte b = raw[pos++];
			dlen |= b & ~0x80;

			if( (b & 0x80) == 0 )
				break;

			dlen <<= 7;
			rawLength++;
		}

		rawLength += dlen;

		byte[] rec = new byte[dlen];	    
		System.arraycopy(raw, pos, rec, 0, dlen);

		return decodeRecord( tag, rec, rawLength );
	}


	private static TaggedTailRecord decodeRecord( byte tag, byte[] rec, int rawLength) {
		switch(tag)
		{
		case 'n':	return new TTR_Number( tag, rec, rawLength );
		case 's':	return new TTR_Signature( tag, rec, rawLength );
		}

		return new TTR_Invalid( tag, rawLength );
	}

	/**
	 * Convert this record to bytes to send out.
	 * @return binary representation.
	 */
	public abstract byte[] toBytes();



	/**
	 * Used by subclasses to encode selves.  
	 * @param tag Tag of record.
	 * @param data Data to put
	 * @return Complete binary representation to attach to packet and send.
	 */
	public static byte[] toBytes( byte tag, byte [] data)
	{
		int len = data.length;

		// lazy impl, supports < 127 len only

		if( (len > 0x7F) || (len < 0 ) )
			throw new MqttUdpRuntimeException(String.format( "TTR too long for tag %X", tag));

		byte [] out = new byte[len+2];

		out[0] = tag;
		out[1] = (byte) ( len & 0x7F );

		System.arraycopy(data, 0, out, 2, len);

		return out;
	}

	static public int htonl(int value) 
	{
		if(ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) 
		{
			return value;
		}
		return Integer.reverseBytes(value);
	}	


}
