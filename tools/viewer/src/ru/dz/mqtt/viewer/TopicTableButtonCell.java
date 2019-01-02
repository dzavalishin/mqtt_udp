package ru.dz.mqtt.viewer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class TopicTableButtonCell extends TableCell<TopicItem, String> {


	//private static final ImageView sendIcon = ImageUtils.getIcon32("semi_success");

	HBox hb = new HBox(); 

	TopicTableButtonCell()
	{
		
		makeButton(ImageUtils.getIcon("options"), "Send to network", new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent t) {
				TopicTableButtonCell cell = TopicTableButtonCell.this;

				// get Selected Item
				//TopicItem currentTopic = (TopicItem) TopicTableButtonCell.this.getTableView().getItems().get(TopicTableButtonCell.this.getIndex());
				TopicItem currentTopic = (TopicItem) cell.getTableView().getItems().get(cell.getIndex());

				//remove selected item from the table list
				//data.remove(currentPerson);
			}
		});
		
		makeButton(ImageUtils.getIcon("order"), "Request from network", new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t) {
			}
		});

		makeToggleButton(ImageUtils.getIcon("keys"), "Send to one host only", new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t) {
			}
		});

		
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

	private void makeToggleButton(ImageView icon, String toolTip, EventHandler<ActionEvent> handler )
	{
		final ToggleButton cellButton = new ToggleButton();
		
		//cellButton.setGraphic(sendIcon);
		cellButton.setGraphic(icon);
		cellButton.setTooltip(new Tooltip(toolTip));
		
		cellButton.setOnAction(handler);
		
		hb.getChildren().add(cellButton);
	}
	
	//Display button if the row is not empty
	@Override
	protected void updateItem(String t, boolean empty) {
		super.updateItem(t, empty);
		if(empty)
		{
			setGraphic(null);
		}
		else
		{
			setGraphic(hb);
		}
	}	

}
