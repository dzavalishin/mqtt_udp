package ru.dz.mqtt_udp;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Usage:
 * <pre>
 * PacketSourceServer ss = new PacketSourceServer();
 * ss.setSink( pkt -> { System.out.println(pkt);});
 * </pre>
 * 
 * <p>Starts automatically.</p>
 * 
 * @author dz
 *
 */
public class PacketSourceServer extends SubServer implements IPacketSource {

	private Consumer<IPacket> sink;

	/**
	 * Starts reception thread.
	 */
	public PacketSourceServer() {		start();	}
		
	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacketSource#setSink(java.util.function.Consumer)
	 */
	@Override
	public void setSink(Consumer<IPacket> sink) {		this.sink = sink;	}

	@Override
	protected void processPacket(IPacket p) throws IOException { if(sink != null ) sink.accept(p);	}
	
}


