package ru.dz.mqtt_udp.proto;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * This TTR represents time and date of this <b>packet</b> (not its payload) creation.
 * In other words, moment when payload was sent, not measured.
 * 
 * <p>
 * 
 * It is added by protocol engine if sending code does not provide measure time.
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
public class TTR_ProtocolTimeDate extends TTR_AbstractTimeDate {

	private final static byte myTag = (byte)'p'; 
	
	public TTR_ProtocolTimeDate(byte tag, byte[] rec, int rawLength) 
	{
		super(tag, rec, rawLength);
	}

	public TTR_ProtocolTimeDate( Instant dt )
	{
		super( myTag, dt );
	}

	public TTR_ProtocolTimeDate( LocalDateTime ldt )
	{
		super( myTag, ldt );
	}
	
}
