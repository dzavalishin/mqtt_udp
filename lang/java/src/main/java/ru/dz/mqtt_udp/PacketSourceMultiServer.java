package ru.dz.mqtt_udp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.items.AbstractItem;

/**
 * 
 * Supports multiple listeners at once.
 * 
 * Usage:
 * <pre>
 * PacketSourceServer ss = new PacketSourceServer();
 * ss.addPacketSink( pkt -&gt; { System.out.println(pkt);});
 * 
 * confing.Provider p = new(); 
 * ss.addPacketSink( p );
 * </pre>
 * 
 * <p>DOES NOT start automatically.</p>
 * 
 * @author dz
 *
 */
public class PacketSourceMultiServer extends SubServer implements IPacketMultiSource {

	//public PacketSourceMultiServer() {
	//}


	private List< Consumer<IPacket> > plist = new ArrayList<>(); 
	private List< Consumer<AbstractItem> > ilist = new ArrayList<>(); 

	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacketMultiSource#addPacketSink(java.util.function.Consumer)
	 */
	@Override
	public void addPacketSink(Consumer<IPacket> sink) {
		synchronized (plist) {
			plist.add(sink);			
		}
	}

	/*
	 * 
	 */
	@Override
	public void removePacketSink(Consumer<IPacket> sink) {
		synchronized (plist) {
			plist.remove(sink);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacketMultiSource#addItemSink(java.util.function.Consumer)
	 */
	@Override
	public void addItemSink(Consumer<AbstractItem> sink) {
		synchronized (ilist) {
			ilist.add(sink);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacketMultiSource#removeItemSink(java.util.function.Consumer)
	 */
	@Override
	public void removeItemSink(Consumer<AbstractItem> sink) {
		synchronized (ilist) {
			ilist.remove(sink);
		}
	}




	@Override
	protected void processPacket(IPacket p) throws IOException {

		//System.out.println("MultiServer got packet "+p);

		synchronized (ilist) {
			if( ilist.size() > 0 )
			{
				AbstractItem ai = AbstractItem.fromPacket(p);
				for( Consumer<AbstractItem> isink : ilist )
				{
					isink.accept(ai);
				}
			}
		}

		synchronized (plist) {
			for( Consumer<IPacket> psink : plist )
			{
				psink.accept(p);
			}
		}

	}

}
