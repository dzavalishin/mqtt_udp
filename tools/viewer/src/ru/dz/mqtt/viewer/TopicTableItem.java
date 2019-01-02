package ru.dz.mqtt.viewer;

public class TopicTableItem extends TopicItem {

	private TableButtonsState tableButtonsState = new TableButtonsState();

	public TopicTableItem(TopicItem src) {
		super(src);
	}

	public TopicTableItem(String topicName) {
		super(topicName);
	}

	public TableButtonsState getTableButtonsState() { return tableButtonsState; }
	public void setTableButtonsState(TableButtonsState tableButtonsState) { this.tableButtonsState = tableButtonsState; }


}
