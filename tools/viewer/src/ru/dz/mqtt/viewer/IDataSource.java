package ru.dz.mqtt.viewer;

import java.util.function.Consumer;

public interface IDataSource {
	void setSink( Consumer<TopicItem> sink );
}
