package ru.dz.mqtt_udp;

import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public interface IPacket {
/*
	public static final int PT_CONNECT = 0x10;
	public static final int PT_CONNACK = 0x20;
	public static final int PT_PUBLISH = 0x30;
	public static final int PT_PUBACK = 0x40;
	public static final int PT_PUBREC = 0x50;
	public static final int PT_PUBREL = 0x60;
	public static final int PT_PUBCOMP = 0x70;
	public static final int PT_SUBSCRIBE = 0x80;
	public static final int PT_SUBACK = 0x90;
	public static final int PT_UNSUBSCRIBE = 0xA0;
	public static final int PT_UNSUBACK = 0xB0;
	public static final int PT_PINGREQ = 0xC0;
	public static final int PT_PINGRESP = 0xD0;
	public static final int PT_DISCONNECT = 0xE0;
*/
	public static final String MQTT_CHARSET = "UTF-8";


	public byte[] toBytes();

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
	    	throw new MqttProtocolException("packet decoded size > packet length");
	    
	    byte[] sub = new byte[slen];	    
	    System.arraycopy(raw, pos, sub, 0, slen);
	    
	    int ptype = 0xF0 & (int)(raw[0]);
	    
		switch(ptype)
		{
		case mqtt_udp_defs.PTYPE_PUBLISH:
			return new PublishPacket(sub, from);

		case mqtt_udp_defs.PTYPE_PINGREQ:
			return new PingReqPacket(sub, from);
			
		case mqtt_udp_defs.PTYPE_PINGRESP:
			return new PingRespPacket(sub, from);
			
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

	
	public static byte[] encodeTotalLength(byte[] pkt, int packetType) {
		int data_len = pkt.length;
		
		byte[] buf = new byte[4]; // can't sent very long packets over UDP, 16 bytes are surely ok
		int bp = 1;
		
		buf[0] = (byte) packetType;
		
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

	
	
}
