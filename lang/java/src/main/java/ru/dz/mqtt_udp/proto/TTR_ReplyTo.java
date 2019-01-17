package ru.dz.mqtt_udp.proto;

/**
 * <p>
 * Number of packet that we reply to.
 * </p>
 * 
 * @author dz
 *
 */
public class TTR_ReplyTo extends TTR_AbstractInteger {

	private final static byte myTag = (byte)'r'; 
		
	public TTR_ReplyTo(byte tag, byte[] rec, int rawLength) 
	{
		super(tag, rec, rawLength);
	}

	public TTR_ReplyTo( int number )
	{
		super( myTag, number);
	}

	


}
