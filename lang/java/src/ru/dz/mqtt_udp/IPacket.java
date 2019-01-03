package ru.dz.mqtt_udp;

import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public interface IPacket {

	public static final String MQTT_CHARSET = "UTF-8";

	public byte[] toBytes();

	public IPacketAddress getFrom();

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
		
	    int dlen = 0;
	    int pos = 1;

	    while(true)
	    {
	        byte b = raw[pos++];
	        dlen |= b & ~0x80;

	        if( (b & 0x80) == 0 )
	            break;

	        dlen <<= 7;
	    }

	    int slen = raw.length - pos;
	    
	    if(slen > dlen) slen = dlen; // TODO log warning
	    
	    if( slen < dlen)
	    	throw new MqttProtocolException("packet decoded size ("+dlen+") > packet length ("+slen+")");
	    
	    byte[] sub = new byte[slen];	    
	    System.arraycopy(raw, pos, sub, 0, slen);
	    
	    int ptype = 0xF0 & (int)(raw[0]);
	    int flags = 0x0F & (int)(raw[0]);
	    
		switch(ptype)
		{
		case mqtt_udp_defs.PTYPE_PUBLISH:
			return new PublishPacket(sub, (byte)flags, from);

		case mqtt_udp_defs.PTYPE_PINGREQ:
			return new PingReqPacket(sub, (byte)flags, from);
			
		case mqtt_udp_defs.PTYPE_PINGRESP:
			return new PingRespPacket(sub, (byte)flags, from);

		case mqtt_udp_defs.PTYPE_SUBSCRIBE:
			return new SubscribePacket(sub, (byte)flags, from);
			
		default:
				throw new MqttProtocolException("Unknown pkt type "+raw[0]);
		}
		
	}

	
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
	 * @param pkt packet payload bytes
	 * @param packetType type ( & 0xF0 )
	 * @param flags flags
	 * @return encoded packet to send to UDP
	 */
	public static byte[] encodeTotalLength(byte[] pkt, int packetType, byte flags) {
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
	    
		return out;
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
	
	public static String getPacketTypeName(int packetType) 
	{
		packetType = packetType >> 4;
	    
		if( (packetType < 0) || (packetType > 15) )
			return "?";
		return pTYpeNames[packetType];
	}
	
	
}
