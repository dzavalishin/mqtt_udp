package ru.dz.mqtt_udp;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Usage:
 * <pre>
 * PacketSourceServer ss = new PacketSourceServer();
 * ss.setSink( pkt -> { System.out.println(pkt);});
 * </pre>
 * @author dz
 *
 */
public class PacketSourceServer extends SubServer implements IPacketSource {

	private Consumer<IPacket> sink;

	public PacketSourceServer() {		start();	}
		
	@Override
	public void setSink(Consumer<IPacket> sink) {		this.sink = sink;	}

	@Override
	protected void processPacket(IPacket p) throws IOException {		sink.accept(p);	}
	
}


