package ru.dz.mqtt.viewer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class TopicTableButtonCell extends TableCell<TopicTableItem, TableButtonsState> {

	//final TopicTableItem tti;
	//private static final ImageView sendIcon = ImageUtils.getIcon32("semi_success");

	private HBox hb = new HBox(); 

	private ToggleButton hostButton;

	//final private ObservableList<TopicTableItem> localData;

	private TableButtonsState tableButtonsState = null;

	private final TopicTable topicTable;
	
	
	
	TopicTableButtonCell(TopicTable topicTable )
	{
		this.topicTable = topicTable;
		makeButton(ImageUtils.getIcon("options"), "Send to network", new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent t) {
				topicTable.sendRecord( getIndex(), tableButtonsState.isLimitSendToHost() );
			}
		});
		
		makeButton(ImageUtils.getIcon("order"), "Request from network", new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t) {
				topicTable.sendRequest( getIndex(), tableButtonsState.isLimitSendToHost() );
			}
		});

		hostButton = makeToggleButton(keysIcon, "Send to one host only", new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t) {
				setLimitSendToHost();
			}
		});
		
		
	}

	private final ImageView keysIcon = ImageUtils.getIcon("keys");
	private final ImageView keyIcon = ImageUtils.getIcon("key");

	
		
	
	protected void setLimitSendToHost() {
		if( tableButtonsState != null )
		{
			tableButtonsState.setLimitSendToHost(hostButton.isSelected());
			setKeysIcon();
		}
	}

	
	protected void getLimitSendToHost() {
		if( tableButtonsState != null )
		{
			hostButton.setSelected(tableButtonsState.isLimitSendToHost());
			setKeysIcon();
		}
	}

	public void setKeysIcon() {
		hostButton.setGraphic(hostButton.isSelected() ? keyIcon : keysIcon);
	}
	
	
	private void makeButton(ImageView icon, String toolTip, EventHandler<ActionEvent> handler )
	{
		final Button cellButton = new Button();
		
		//cellButton.setGraphic(sendIcon);
		cellButton.setGraphic(icon);
		cellButton.setTooltip(new Tooltip(toolTip));
		
		cellButton.setOnAction(handler);
		
		hb.getChildren().add(cellButton);
	}

	private ToggleButton makeToggleButton(ImageView icon, String toolTip, EventHandler<ActionEvent> handler )
	{
		final ToggleButton cellButton = new ToggleButton();
		
		//cellButton.setGraphic(sendIcon);
		cellButton.setGraphic(icon);
		cellButton.setTooltip(new Tooltip(toolTip));
		
		cellButton.setOnAction(handler);
		
		hb.getChildren().add(cellButton);
		return cellButton;
	}
	
	//Display button if the row is not empty
	@Override
	protected void updateItem(TableButtonsState t, boolean empty) {
		this.tableButtonsState  = t;
		super.updateItem(t, empty);
		if(empty)
		{
			setGraphic(null);
		}
		else
		{
			setGraphic(hb);
			getLimitSendToHost();
		}
	}	

}
