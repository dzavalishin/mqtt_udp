package ru.dz.mqtt_udp.proto;

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
		//case 's':	return new TTR_Signature( tag, rec, rawLength );
		}
		
		return new TTR_Invalid( tag, rawLength );
	}


	
}
