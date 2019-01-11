package ru.dz.mqtt_udp;

import java.util.function.Consumer;

import ru.dz.mqtt_udp.items.AbstractItem;

/**
 * Supports multiple listeners for incoming packets.
 * @author dz
 *
 */
public interface IPacketMultiSource 
{
	/**
	 * Add sink to consume received packets.
	 * @param sink Consumer<IPacket> to add to consumers list.
	 */
	public void addPacketSink(Consumer<IPacket> sink);	
	/**
	 * Remove sink from consumers list.
	 * @param sink Consumer<IPacket> to add to consumers list.
	 */
	public void removePacketSink(Consumer<IPacket> sink);

	
	
	
	/**
	 * Add sink to consume received packets.
	 * @param sink Consumer<AbstractItem> to add to consumers list.
	 */
	public void addItemSink(Consumer<AbstractItem> sink);	
	/**
	 * Remove sink from consumers list.
	 * @param sink Consumer<AbstractItem> to add to consumers list.
	 */
	public void removeItemSink(Consumer<AbstractItem> sink);
	
	
}
