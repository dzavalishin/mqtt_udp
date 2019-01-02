package ru.dz.mqtt.viewer;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TopicTable  {

	private Stage newWindow = new Stage();
	private TableView<TopicItem> table = new TableView<>();

	private boolean topicListUpdateEnabled = true;
	private boolean autoNetworkSendEnabled = false;
	
	public TopicTable(ObservableList<TopicItem> data) {
		table.setEditable(true);

        TableColumn<TopicItem, String> buttonsCol = new TableColumn("");
		TableColumn<TopicItem, String> topicCol = new TableColumn("Topic");
        TableColumn<TopicItem, String> valueCol = new TableColumn(); // new TableColumn("Value");
        TableColumn<TopicItem, String> hostCol = new TableColumn("Host");
        TableColumn<TopicItem, String> timeCol = new TableColumn(); // new TableColumn("Time");

        Label timeLabel = new Label("Time");
        timeLabel.setTooltip(new Tooltip("Time of last update"));
        timeCol.setGraphic(timeLabel);
        
        Label valueLabel = new Label("Value");
        valueLabel.setTooltip(new Tooltip("Dobleclick value to update"));
        valueCol.setGraphic(valueLabel);
        
        
        
        topicCol.setMinWidth(140);
        valueCol.setMinWidth(140);
        
        /*
        buttonsCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
        topicCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
        valueCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
        hostCol.prefWidthProperty().bind(table.widthProperty().divide(8)); // w * 1/8
        timeCol.prefWidthProperty().bind(table.widthProperty().divide(8)); // w * 1/4
		*/

        // See https://stackoverflow.com/questions/10152828/javafx-2-automatic-column-width/10152992
        table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
        
        
        topicCol.setEditable(false);
        buttonsCol.setEditable(false);
        hostCol.setEditable(false);
        timeCol.setEditable(false);
        
        
        valueCol.setEditable(true);
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(
                new EventHandler<CellEditEvent<TopicItem, String>>() {
                    @Override
                    public void handle(CellEditEvent<TopicItem, String> t) {
                        ((TopicItem) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setValue(t.getNewValue());
                        if( autoNetworkSendEnabled )
                        {
                        // TODO if autosend enabled - send,
                        }
                        else
                        {
                        // TODO else enable send button in row
                        }
                    }
                }
            );

        
        topicCol.setCellValueFactory(new PropertyValueFactory<TopicItem, String>("topic"));
        valueCol.setCellValueFactory(new PropertyValueFactory<TopicItem, String>("value"));
        hostCol.setCellValueFactory(new PropertyValueFactory<TopicItem, String>("from"));
        timeCol.setCellValueFactory(new PropertyValueFactory<TopicItem, String>("time"));
        
        table.getColumns().addAll(buttonsCol, topicCol, valueCol, hostCol, timeCol);
 		
        
        ObservableList<TopicItem> localData = FXCollections.observableArrayList( data );
        table.setItems(localData);
        data.addListener(new ListChangeListener<TopicItem>(){

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends TopicItem> c) {
				if( topicListUpdateEnabled )
				{
					//localData = FXCollections.observableArrayList( data );
					//table.setItems( localData );
					table.setItems( FXCollections.observableArrayList( data ) );
				}
			}
        	
        });

        
        openWindow();
	}

	private static final Image windowIcon = ImageUtils.getImage("content256.png");

	private void openWindow() {
		
		VBox vbox = new VBox(makeToolBar(),table);
		vbox.setFillWidth(true);

		
		Scene secondScene = new Scene(vbox, 800, 500);

		
        // New window (Stage)
        newWindow.setTitle("Topic editor");
        newWindow.setScene(secondScene);

        // Set position of second window, related to primary window.
        //newWindow.setX(primaryStage.getX() + 200);
        //newWindow.setY(primaryStage.getY() + 100);

        newWindow.setMinWidth(500);
        
        newWindow.getIcons().add(windowIcon);

        //newWindow.show();
        }
	
	public void setVisible(boolean is)
	{
		if( is ) newWindow.show();
		else newWindow.hide();
	}


	private static final ImageView lockedIcon = ImageUtils.getIcon32("locked");
	private static final ImageView unlockedIcon = ImageUtils.getIcon32("unlocked");
	private static final ImageView newTopicIcon = ImageUtils.getIcon32("tests");
	
	private HBox makeToolBar()
	{
		
		Button newTopicButton = new Button();
		newTopicButton.setTooltip(new Tooltip("Add new topic to the list"));
		newTopicButton.setGraphic(newTopicIcon);
		newTopicButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				Optional<String> result = newTopicDialog();
				result.ifPresent(newTopicName -> { System.out.println(newTopicName);	});
			} // TODO
		});

		
		Button searchButton = new Button();
		searchButton.setTooltip(new Tooltip("Search for topic"));
		searchButton.setGraphic(ImageUtils.getIcon32("analysis"));
		searchButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {  } // TODO
		});

		ToggleButton refreshListButton = new ToggleButton();
		refreshListButton.setTooltip(new Tooltip("Enable automatic topic list refresh"));
		refreshListButton.setGraphic(unlockedIcon);
		refreshListButton.setSelected(true);
		refreshListButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { 
				topicListUpdateEnabled = refreshListButton.isSelected(); 
				refreshListButton.setGraphic(topicListUpdateEnabled ? unlockedIcon : lockedIcon);
			} // TODO
		});
		
		ToggleButton refreshValueButton = new ToggleButton();
		refreshValueButton.setTooltip(new Tooltip("Enable automatic value send"));
		refreshValueButton.setGraphic(ImageUtils.getIcon32("refresh"));
		refreshValueButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { autoNetworkSendEnabled = refreshValueButton.isSelected(); } // TODO
		});
		
		
		ToolBar leftTb = new ToolBar();
		leftTb.getItems().addAll(newTopicButton,searchButton);

		
		ToolBar rightTb = new ToolBar();
		rightTb.getItems().addAll(refreshListButton,refreshValueButton);
		
		Region spacer = new Region();
		spacer.getStyleClass().add("menu-bar");
		HBox.setHgrow(spacer, Priority.SOMETIMES);
		
		
		HBox hbox = new HBox(leftTb,spacer,rightTb);
		
		return hbox;
	}

	
	TextInputDialog td = new TextInputDialog();
	
	{
		Stage stage = (Stage) td.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ImageUtils.getImage("tests256.png"));
		td.setTitle("Create new topic");
		td.setHeaderText("New topic name");
	}
	
	private Optional<String> newTopicDialog()
	{
		//String ret = null;
		
		//td.setContentText("");
		//td.setGraphic(newTopicIcon);
		td.setGraphic(ImageUtils.getIcon32("tests"));
		//td.getDialogPane().setGraphic(newTopicIcon);
		
		return td.showAndWait();
	}

}
