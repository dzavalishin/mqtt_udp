package ru.dz.mqtt_udp.proto;


/**
 * <p>
 * Abstract record with one 64 bit integer in payload. 
 * </p>
 * 
 * @author dz
 *
 */
public class TTR_AbstractInteger64 extends TaggedTailRecord {
	
	private long value;

	public TTR_AbstractInteger64(byte tag, byte[] rec, int rawLength) 
	{
		super(tag,rawLength);

		long n = 0;
		
		for( int i = 0; i < Long.BYTES; i++ )
		{
			n |= ((int)rec[i]) << (8*(Long.BYTES - i - 1));
		}
		
		value = n;
	}
	
	public TTR_AbstractInteger64( byte tag, long number )
	{
		super( tag, -1);
		value = number;		
	}
	

	@Override
	public byte[] toBytes() {
		byte [] out = new byte[Long.BYTES];
		
		for( int i = 0; i < Long.BYTES; i++ )
		{
			out[i] = (byte)(value >> (8*(Long.BYTES - i - 1)) );
		}
		
		return toBytes(tag, out);
	}
	

	/**
	 * 
	 * @return Record payload long.
	 */
	public long getValue() {
		return value;
	}
	
	
}
