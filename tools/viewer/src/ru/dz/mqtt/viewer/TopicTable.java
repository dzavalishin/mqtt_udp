package ru.dz.mqtt.viewer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TopicTable  {

	private Stage newWindow = new Stage();
	private TableView<TopicItem> table = new TableView<>();
	
	
	public TopicTable() {
		table.setEditable(true);

        TableColumn buttonsCol = new TableColumn("");
		TableColumn topicCol = new TableColumn("Topic");
        TableColumn valueCol = new TableColumn("Value");
        
        topicCol.setEditable(false);
        buttonsCol.setEditable(false);
        
        //buttonsCol.setCellFactory(new );
        
        table.getColumns().addAll(buttonsCol, topicCol, valueCol);
 		
        openWindow();
	}


	private void openWindow() {
		
		VBox vbox = new VBox(makeToolBar(),table);
		vbox.setFillWidth(true);

		
		Scene secondScene = new Scene(vbox, 600, 400);
		 
        // New window (Stage)
        newWindow.setTitle("Topic editor");
        newWindow.setScene(secondScene);

        // Set position of second window, related to primary window.
        //newWindow.setX(primaryStage.getX() + 200);
        //newWindow.setY(primaryStage.getY() + 100);

        //newWindow.show();
        }
	
	public void setVisible(boolean is)
	{
		if( is ) newWindow.show();
		else newWindow.hide();
	}


	
	private HBox makeToolBar()
	{
		
		Button newTopicButton = new Button();
		newTopicButton.setTooltip(new Tooltip("Add new topic to the list"));
		newTopicButton.setGraphic(ImageUtils.getIcon32("tests"));
		newTopicButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {  } // TODO
		});

		
		Button searchButton = new Button();
		searchButton.setTooltip(new Tooltip("Search for topic"));
		searchButton.setGraphic(ImageUtils.getIcon32("analysis"));
		searchButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {  } // TODO
		});

		ToggleButton refreshButton = new ToggleButton();
		refreshButton.setTooltip(new Tooltip("Enable automatic value send"));
		refreshButton.setGraphic(ImageUtils.getIcon32("refresh"));
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {  } // TODO
		});
		
		
		ToolBar leftTb = new ToolBar();
		leftTb.getItems().addAll(newTopicButton,searchButton);

		
		ToolBar rightTb = new ToolBar();
		rightTb.getItems().addAll(refreshButton);
		
		Region spacer = new Region();
		spacer.getStyleClass().add("menu-bar");
		HBox.setHgrow(spacer, Priority.SOMETIMES);
		
		
		HBox hbox = new HBox(leftTb,spacer,rightTb);
		
		return hbox;
	}
	

}
