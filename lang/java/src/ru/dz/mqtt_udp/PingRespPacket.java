package ru.dz.mqtt_udp;


import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public class PingRespPacket extends GenericPacket {

	public PingRespPacket(byte[] raw, byte flags, IPacketAddress from) {
		this.flags = flags;
		this.from = from;
		if( raw.length > 0 )
			System.err.println("nonempty PingRespPacket");
	}

	public PingRespPacket() {
		
	}

	@Override
	public byte[] toBytes() {
		byte[] pkt = new byte[0];
		return IPacket.encodeTotalLength(pkt, mqtt_udp_defs.PTYPE_PINGRESP, flags );	
	}

	@Override
	public String toString() {		
		return String.format("MQTT/UDP PING Responce" );
	}
	

}
