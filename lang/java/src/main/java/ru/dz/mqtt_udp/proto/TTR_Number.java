package ru.dz.mqtt_udp.proto;

import java.util.concurrent.atomic.AtomicInteger;

public class TTR_Number extends TTR_AbstractInteger {

	private final static byte myTag = (byte)'n'; 
	
	//private final int packetNumber;
	private static AtomicInteger next = new AtomicInteger( (int)(System.currentTimeMillis() & 0xFFFFFFFF) );
	
	public TTR_Number(byte tag, byte[] rec, int rawLength) 
	{
		super(tag, rec, rawLength);
	}

	public TTR_Number( int number )
	{
		super( myTag, number);
	}

	/** use next sequential number */
	public TTR_Number()
	{
		super( myTag, next.incrementAndGet() );
	}
	
	


}
