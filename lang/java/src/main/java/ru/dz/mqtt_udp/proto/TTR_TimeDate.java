package ru.dz.mqtt_udp.proto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * <p>
 * 
 * This TTR represents time and date of this packet's <b>payload</b> creation.
 * 
 * <p>
 * 
 * It is supposed to be used in processing and storage algorithms. For example,
 * It should be used as time reference when drawing diagrams or calculating
 * derivative.
 * 
 * <p>
 * 
 * Note that binary 64 bit representation is in msec, like unix <b>time_t * 1000</b>.
 * Actual accuracy is, of course, depends on time source available for sender.
 * 
 * <p>
 * 
 * <b>NB!</b> This is <b>LOCAL</b> time.
 * 
 * @author dz
 *
 */
public class TTR_TimeDate extends TTR_AbstractInteger64 {

	private final static byte myTag = (byte)'t'; 
	
	public TTR_TimeDate(byte tag, byte[] rec, int rawLength) 
	{
		super(tag, rec, rawLength);
	}

	/*
	public TTR_TimeDate( long number )
	{
		super( myTag, number);
	}*/

	public TTR_TimeDate( Instant dt )
	{
		super( myTag, dt.toEpochMilli() );
	}

	public TTR_TimeDate( LocalDateTime ldt )
	{
		//Instant dt = ldt.toInstant(ZoneOffset.UTC) 
		//super( myTag, dt.toEpochMilli() );
		super( myTag, ldt.toInstant(ZoneOffset.UTC).toEpochMilli() );
	}
	
	/**
	 * Convert to Instant time/date representation.
	 * @return Time/date
	 */
	public Instant getInstant() 
	{
		return Instant.ofEpochMilli(getValue());
	}
	
	public LocalDateTime getLocalDateTime() {
		return LocalDateTime.ofInstant(getInstant(), ZoneId.ofOffset("", ZoneOffset.UTC));
	}
}
