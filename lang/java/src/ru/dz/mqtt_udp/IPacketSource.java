package ru.dz.mqtt_udp;

import java.util.function.Consumer;

public interface IPacketSource {
	void setSink( Consumer<IPacket> sink );

}
