package ru.dz.mqtt_udp.proto;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * This TTR represents time and date of this packet's <b>payload</b> creation.
 * In other words, moment when payload was measured, not sent.
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
 * <b>NB!</b> This is <b>LOCAL</b> time, not UTC/GMT.
 * 
 * @author dz
 *
 */
public class TTR_MeasureTimeDate extends TTR_AbstractTimeDate {

	private final static byte myTag = (byte)'t'; 
	
	public TTR_MeasureTimeDate(byte tag, byte[] rec, int rawLength) 
	{
		super(tag, rec, rawLength);
	}

	public TTR_MeasureTimeDate( Instant dt )
	{
		super( myTag, dt );
	}

	public TTR_MeasureTimeDate( LocalDateTime ldt )
	{
		super( myTag, ldt );
	}
	
}
