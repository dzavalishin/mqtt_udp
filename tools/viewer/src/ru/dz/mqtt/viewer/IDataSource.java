package ru.dz.mqtt.viewer;

import java.util.function.Consumer;

import ru.dz.mqtt_udp.items.AbstractItem;

@Deprecated
public interface IDataSource {
	void setSink( Consumer<AbstractItem> sink );
}
