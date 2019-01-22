package ru.dz.mqtt_udp.proto;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import ru.dz.mqtt_udp.util.MqttUdpRuntimeException;

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


	/**
	 * <p>Decode tail of packet, all the attached TTRs.
	 * 
	 * <p>Finds out and returns position of Signature TTR, so that outside code can
	 * check if packet signed ok. Outside code must calculate signature locally
	 * for part of the packet preceding signature TTR. Including, of course,
	 * classic MQTT packet part.
	 * 
	 * @param raw Tail of the packet, everything after the classic MQTT payload size.
	 * @param signaturePos (Return!) Position of signature TTR in raw.  
	 * @return Collection of TTRs discovered.
	 */
	public static Collection<TaggedTailRecord> fromBytesAll(  byte[] raw, AtomicReference<Integer> signaturePos )
	{
		ArrayList<TaggedTailRecord> out = new ArrayList<>();
		
		int sig_pos = -1;
		
		final int len = raw.length;
		int eaten = 0;
		
		while( (len - eaten) > 0)
		{
			int tailLen = len-eaten;
			byte[] tail = new byte[tailLen];
			System.arraycopy(raw, eaten, tail, 0, tailLen);
			TaggedTailRecord ttr = fromBytes(tail);
			out.add(ttr);
			
			if( ttr instanceof TTR_Signature )
			{
				// We need to record signature position to be able to
				// calculate local signature, which is calculated for
				// part of the packet preceding signature TTR.
				//
				// NB!
				// Size of classical MQTT packet must be added outside.
				//
				sig_pos = eaten;
			}
			
			eaten += ttr.getRawLength();
		}
		
		signaturePos.set(sig_pos);
		return out;
	}
	
	
	public static TaggedTailRecord fromBytes(  byte[] raw )
	{
		int rawLength = 1; // tag
		byte tag = raw[0];

		int dlen = 0;
		int pos = 1;

		while(true)
		{
			rawLength++;

			byte b = raw[pos++];
			dlen |= b & ~0x80;

			if( (b & 0x80) == 0 )
				break;

			dlen <<= 7;
		}

		rawLength += dlen;

		byte[] rec = new byte[dlen];	    
		System.arraycopy(raw, pos, rec, 0, dlen);

		return decodeRecord( tag, rec, rawLength );
	}


	private static TaggedTailRecord decodeRecord( byte tag, byte[] rec, int rawLength) {
		switch(tag)
		{
		case 'n':	return new TTR_PacketNumber( tag, rec, rawLength );
		case 's':	return new TTR_Signature( tag, rec, rawLength );
		default: break;
		}

		return new TTR_Invalid( tag, rawLength );
	}

	/**
	 * Convert this record to bytes to send out.
	 * @return binary representation.
	 */
	public abstract byte[] toBytes();



	/**
	 * Used by subclasses to encode selves.  
	 * @param tag Tag of record.
	 * @param data Data to put
	 * @return Complete binary representation to attach to packet and send.
	 */
	public static byte[] toBytes( byte tag, byte [] data)
	{
		int len = data.length;

		// lazy impl, supports < 127 len only

		if( (len > 0x7F) || (len < 0 ) )
			throw new MqttUdpRuntimeException(String.format( "TTR too long for tag %X", tag));

		byte [] out = new byte[len+2];

		out[0] = tag;
		out[1] = (byte) ( len & 0x7F );

		System.arraycopy(data, 0, out, 2, len);

		return out;
	}

	static public int htonl(int value) 
	{
		if(ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) 
		{
			return value;
		}
		return Integer.reverseBytes(value);
	}	


	/*
	public static ArrayList<TaggedTailRecord> preprocessBeforeSend( AbstractCollection<TaggedTailRecord> in )
	{
		ArrayList<TaggedTailRecord> out = new ArrayList<>(in.size());
		
		TaggedTailRecord sig = null;
		boolean haveNumber = false;
		
		if( in != null )
			for( TaggedTailRecord r : in )
		{
			if( r instanceof TTR_Signature )
			{
				sig = r;
				continue;
			}

			if( r instanceof TTR_PacketNumber )
				haveNumber = true;
			
			out.add(r);
		}
		
		// Add packet number to list, if none
		if( !haveNumber )
			out.add(new TTR_PacketNumber());
		
		// Signature must be last one - NO, it is impossible for signature to be here
		if( sig != null )
			//out.add(sig);
			//throw new MqttUdpRuntimeException("Signature must be generated later");
			GlobalErrorHandler.handleError(ErrorType.Protocol, "Signature must be generated later");
		
		return out;
	}
	*/
}
