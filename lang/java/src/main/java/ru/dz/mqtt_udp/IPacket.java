package ru.dz.mqtt_udp;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import ru.dz.mqtt_udp.hmac.HMAC;
import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.proto.TTR_PacketNumber;
import ru.dz.mqtt_udp.proto.TTR_Signature;
import ru.dz.mqtt_udp.proto.TaggedTailRecord;
import ru.dz.mqtt_udp.util.ByteArray;
import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

/**
 * Interface of general MQTT/UDP packet.
 * @author dz
 *
 */

public interface IPacket {

	/** MQTT/UDP character set */
	public static final String MQTT_CHARSET = "UTF-8";

	/**
	 * Generate network representation of packet to be sent.
	 * @return UDP packet contents.
	 */
	public byte[] toBytes();

	/**
	 * Get packet sender address.
	 * @return IP address.
	 */
	public IPacketAddress getFrom();

	/**
	 * Get packet type byte, as sent over the net (&amp; 0xF0).
	 * @return Packet type byte.
	 */
	public int getType();

	
	
	
	
	
	
	
	/**
	 * Construct packet object from binary data (recvd from net).
	 * @param raw binary data from UDP packet
	 * @param from source address
	 * @return Packet object
	 * @throws MqttProtocolException on incorrect binary packet data
	 */
	public static IPacket fromBytes( byte[] raw, IPacketAddress from ) throws MqttProtocolException
	{		
	    int total_len = 0;
	    int pos = 1;

	    while(true)
	    {
	        byte b = raw[pos++];
	        total_len |= b & ~0x80;

	        if( (b & 0x80) == 0 )
	            break;

	        total_len <<= 7;
	    }

	    int slen = raw.length - pos;
	    
	    Collection<TaggedTailRecord> ttrs = null;
	    if(slen > total_len) 
	    {
	    	int tail_len = slen - total_len; 
	    	byte [] ttrs_bytes = new byte[tail_len];
	    	slen = total_len; 

	    	System.arraycopy(raw, total_len+pos, ttrs_bytes, 0, tail_len);
	    	
	    	AtomicReference<Integer> signaturePos = new AtomicReference<Integer>(-1);
			ttrs = TaggedTailRecord.fromBytesAll(ttrs_bytes, signaturePos);
			
			/*{
				for( TaggedTailRecord ttr : ttrs )
					System.out.println(ttr);
			}*/
			
			int sigPos = signaturePos.get();
			if(sigPos >= 0)
			{
				sigPos += total_len;
				sigPos += pos;
				// We have signature in packet we got, and we know its position 
				// in incoming packet. Calculate ours and check.

				byte [] sig_check_bytes = new byte[sigPos];
				System.arraycopy(raw, 0, sig_check_bytes, 0, sigPos);
				
				/*if(true)
				{
					byte[] our_signature = HMAC.hmacDigestMD5(sig_check_bytes, "signPassword"); // TODO get password
					ByteArray.dumpBytes("our", our_signature);
				}*/
				
				for( TaggedTailRecord ttr : ttrs )
				{
					if (ttr instanceof TTR_Signature) {
						TTR_Signature ts = (TTR_Signature) ttr;

						//ByteArray.dumpBytes("his", ts.getSignature());

						
						boolean sigCorrect = ts.check(sig_check_bytes, "signPassword"); // TODO get password
						if(!sigCorrect)
							throw new MqttProtocolException("Incorrect packet signature");
						break;
					}
				}
			}
	    }
	    
	    if( slen < total_len)
	    	throw new MqttProtocolException("packet decoded size ("+total_len+") > packet length ("+slen+")");
	    
	    byte[] sub = new byte[slen];	    
	    System.arraycopy(raw, pos, sub, 0, slen);
	    
	    int ptype = 0xF0 & (int)(raw[0]);
	    int flags = 0x0F & (int)(raw[0]);
	    
	    GenericPacket p;
		switch(ptype)
		{
		case mqtt_udp_defs.PTYPE_PUBLISH:
			p = new PublishPacket(sub, (byte)flags, from);
			break;

		case mqtt_udp_defs.PTYPE_PINGREQ:
			p = new PingReqPacket(sub, (byte)flags, from);
			break;
			
		case mqtt_udp_defs.PTYPE_PINGRESP:
			p = new PingRespPacket(sub, (byte)flags, from);
			break;

		case mqtt_udp_defs.PTYPE_SUBSCRIBE:
			p = new SubscribePacket(sub, (byte)flags, from);
			break;
			
		default:
				throw new MqttProtocolException("Unknown pkt type "+raw[0]);
		}
		
		return p.applyTTRs(ttrs);
	}

	

	/**
	 * Decode 2-byte string length.
	 * @param pkt Binary packet data.
	 * @return Decoded length.
	 */
	public static int decodeTopicLen( byte [] pkt )
	{
	    int ret = 0;

	    //ret = (pkt[1] << 8) | pkt[0];
	    ret = (pkt[0] << 8) | pkt[1];

	    ret &= 0xFFFF;
	    
	    return ret;
	}

	/**
	 * Rename to encodePacketHeader?
	 * 
	 * Encode total packet length. Encoded as variable length byte sequence, 7 bits per byte.
	 * 
	 * @param pkt packet payload bytes
	 * @param packetType type ( &amp; 0xF0 )
	 * @param flags flags
	 * @return encoded packet to send to UDP
	 */
	public static byte[] encodeTotalLength(byte[] pkt, int packetType, byte flags, AbstractCollection<TaggedTailRecord> ttr ) {
		int data_len = pkt.length;
		
		byte[] buf = new byte[4]; // can't sent very long packets over UDP, 16 bytes are surely ok
		int bp = 1;
		
		buf[0] = (byte) ((packetType & 0xF0) | (flags & 0x0F));
		
	    do 
	    {
	        byte b = (byte) (data_len % 128);
	        data_len /= 128;

	        if( data_len > 0 )
	            b |= 0x80;

	        buf[bp++] = b;
	    } while( data_len > 0 );

	    int tlen = pkt.length + bp;

	    byte[] out = new byte[tlen];
	    
	    System.arraycopy(buf, 0, out, 0, bp);
	    System.arraycopy(pkt, 0, out, bp, pkt.length );
	    
	    // Encode in Tagged Tail Records - packet extensions
	    byte[] ttrbin = encodeTTR( ttr, out );
	    //byte[] ttrbin = out;
	    
	    
		//return out;
		return ttrbin;
	}

	/**
	 * <p>Have 'classic' MQTT packet at input, extend it with Tagged Tail Records.</p>
	 * 
	 * <p>Add packet number if one is missing. Add signature.</p>
	 * 
	 * @param ttrs Collection of TTRs to add.
	 * @param packetBeginning Classic packet.
	 * @return Extended packet.
	 */
	public static byte[] encodeTTR( AbstractCollection<TaggedTailRecord> ttrs, byte[] packetBeginning ) 
	{
		ArrayList<byte[]> outs = new ArrayList<>();

		boolean haveNumber = false;
		
		if( ttrs != null )
			for( TaggedTailRecord r : ttrs )
		{
			if( r instanceof TTR_Signature )
			{
				GlobalErrorHandler.handleError(ErrorType.Protocol, "Signature must be generated here");
				continue;
			}

			if( r instanceof TTR_PacketNumber )
				haveNumber = true;

			outs.add(r.toBytes());
		}

		// Add packet number to list, if none
		if( !haveNumber )
			outs.add(new TTR_PacketNumber().toBytes());

		int totalLen = packetBeginning.length;
		for( byte[] bb : outs )
		{
			totalLen += bb.length;
		}
		
		byte [] presig = new byte[totalLen+TTR_Signature.SIGLEN];
		//byte [] presig = new byte[totalLen];
		
		System.arraycopy(packetBeginning, 0, presig, 0, packetBeginning.length);

		int pos = packetBeginning.length;
		for( byte[] bb : outs )
		{
			System.arraycopy(bb, 0, presig, pos, bb.length);
			pos += bb.length;
		}

		byte [] toSign = new byte[totalLen];
		System.arraycopy(presig, 0, toSign, 0, totalLen);
		
		//byte[] signature = HMAC.hmacDigestMD5(presig, "signPassword"); // TODO get password!
		byte[] signature = HMAC.hmacDigestMD5(toSign, "signPassword"); // TODO get password!
		
		TTR_Signature sig = new TTR_Signature(signature);

		byte[] sigBytes = sig.toBytes();
		//System.arraycopy( sigBytes, 0, presig, pos, TTR_Signature.SIGLEN);
		System.arraycopy( sigBytes, 0, presig, pos, TTR_Signature.SIGLEN);
		
		return presig;
	}


	static final String[] pTYpeNames = {
			"? NULL",
			"Connect",
			"ConnAck",
			"Publish",
			"PubAck",
			"PubRec",
			"PubRel",
			"PubComp",
			"Subscribe",
			"SubAck",
			"UnSubscribe",
			"UnSubAck",
			"PingReq",
			"PingResp",
			"Disconnect",
			"? 0xFF",
	};
	
	/**
	 * Get packet type name.
	 * @param packetType as in incoming byte (&amp; 0xF0).
	 * @return Type string.
	 */
	public static String getPacketTypeName(int packetType) 
	{
		int pos = packetType >> 4;
	    
		if( (pos < 0) || (pos > 15) )
			return "?";
		return pTYpeNames[pos];
	}
	
	
}
