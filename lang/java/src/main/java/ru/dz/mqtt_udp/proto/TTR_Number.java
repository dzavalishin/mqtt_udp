package ru.dz.mqtt_udp.proto;

public class TTR_Number extends TaggedTailRecord {

	private final int packetNumber;
	private int next = (int)(System.currentTimeMillis() & 0xFFFFFFFF);
	
	public TTR_Number(byte tag, byte[] rec, int rawLength) 
	{
		super(tag,rawLength);

		int n = 0;
		
		for( int i = 0; i < Integer.BYTES; i++ )
		{
			n |= ((int)rec[i]) << (8*(Integer.BYTES - i - 1));
		}
		
		packetNumber = n;
	}

	public TTR_Number( int number )
	{
		super( (byte)'n', -1);
		packetNumber = number;		
	}

	/** use next sequential number */
	public TTR_Number()
	{
		super( (byte)'n', -1);
		packetNumber = next++;		
	}
	
	
	@Override
	public byte[] toBytes() {
		byte [] out = new byte[Integer.BYTES];
		
		for( int i = 0; i < Integer.BYTES; i++ )
		{
			out[i] = (byte)(packetNumber >> (8*(Integer.BYTES - i - 1)) );
		}
		
		return toBytes(tag, out);
	}

	public int getPacketNumber() {
		return packetNumber;
	}

}
