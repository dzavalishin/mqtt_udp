package ru.dz.mqtt_udp.proto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * <p>
 * 
 * Abstract TTR carrying time/date with millisecond accuracy.
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
public class TTR_AbstractTimeDate extends TTR_AbstractInteger64 {

	public TTR_AbstractTimeDate(byte tag, byte[] rec, int rawLength) 
	{
		super(tag, rec, rawLength);
	}

	
	protected TTR_AbstractTimeDate( byte myTag, long number )
	{
		super( myTag, number);
	}

	public TTR_AbstractTimeDate( byte myTag, Instant dt )
	{
		super( myTag, dt.toEpochMilli() );
	}

	public TTR_AbstractTimeDate( byte myTag, LocalDateTime ldt )
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
