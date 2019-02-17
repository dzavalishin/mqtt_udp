package ru.dz.mqtt_udp;

import java.util.function.Consumer;

@Deprecated
public interface IPacketSource {
	/**
	 * Set sink to consume packets.
	 * @param sink Set consumer.
	 */
	void setSink( Consumer<IPacket> sink );

}
