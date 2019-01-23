package ru.dz.mqtt_udp.proto;


/**
 * <p>
 * Abstract record with one integer in payload. 
 * </p>
 * 
 * @author dz
 *
 */
public class TTR_AbstractInteger32 extends TaggedTailRecord {

	
	private int value;

	public TTR_AbstractInteger32(byte tag, byte[] rec, int rawLength) 
	{
		super(tag,rawLength);

		int n = 0;
		
		for( int i = 0; i < Integer.BYTES; i++ )
		{
			n |= ((int)rec[i]) << (8*(Integer.BYTES - i - 1));
		}
		
		value = n;
	}
	
	public TTR_AbstractInteger32( byte tag, int number )
	{
		super( tag, -1);
		value = number;		
	}
	

	@Override
	public byte[] toBytes() {
		byte [] out = new byte[Integer.BYTES];
		
		for( int i = 0; i < Integer.BYTES; i++ )
		{
			out[i] = (byte)(value >> (8*(Integer.BYTES - i - 1)) );
		}
		
		return toBytes(tag, out);
	}
	

	/**
	 * 
	 * @return Record payload integer.
	 */
	public int getValue() {
		return value;
	}
	
	
}
