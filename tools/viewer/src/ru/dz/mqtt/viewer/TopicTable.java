package ru.dz.mqtt.viewer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
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
import javafx.util.Callback;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;

public class TopicTable  {

	private Stage newWindow = new Stage();

	private ObservableList<TopicTableItem> localData;
	private TableView<TopicTableItem> table = new TableView<>();

	private boolean topicListUpdateEnabled = true;
	private boolean autoNetworkSendEnabled = false;

	public TopicTable(ObservableList<TopicItem> data) {
		table.setEditable(true);

		TableColumn<TopicTableItem, TableButtonsState> buttonsCol = new TableColumn<TopicTableItem, TableButtonsState>("");
		TableColumn<TopicTableItem, String> topicCol = new TableColumn<TopicTableItem, String>("Topic");
		TableColumn<TopicTableItem, String> valueCol = new TableColumn<TopicTableItem, String>(); // new TableColumn("Value");
		TableColumn<TopicTableItem, String> hostCol = new TableColumn<TopicTableItem, String>("Host");
		TableColumn<TopicTableItem, String> timeCol = new TableColumn<TopicTableItem, String>(); // new TableColumn("Time");

		Label timeLabel = new Label("Time");
		timeLabel.setTooltip(new Tooltip("Time of last update"));
		timeCol.setGraphic(timeLabel);

		Label valueLabel = new Label("Value");
		valueLabel.setTooltip(new Tooltip("Dobleclick value to update"));
		valueCol.setGraphic(valueLabel);


		buttonsCol.setMaxWidth(104);
		buttonsCol.setMinWidth(104);
		buttonsCol.setResizable(false);

		hostCol.setMinWidth(100);
		hostCol.setMaxWidth(100);
		hostCol.setResizable(false);

		timeCol.setMinWidth(100);
		timeCol.setMaxWidth(100);
		timeCol.setResizable(false);

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
				new EventHandler<CellEditEvent<TopicTableItem, String>>() {
					@Override
					public void handle(CellEditEvent<TopicTableItem, String> t) {
						int row = t.getTablePosition().getRow();
						ObservableList<TopicTableItem> items = t.getTableView().getItems();
						//((TopicTableItem) items.get(row)).setValue(t.getNewValue());
						TopicTableItem item = items.get(row);
						item.setValue(t.getNewValue());
						if( autoNetworkSendEnabled )
						{
							sendRecord(row, item.getTableButtonsState().isLimitSendToHost());
						}
						/*else
						{
							// TODO else enable send button in row
						}*/
					}
				}
				);


		buttonsCol.setCellFactory(
				new Callback<TableColumn<TopicTableItem, TableButtonsState>, TableCell<TopicTableItem, TableButtonsState>>() {
					@Override
					public TableCell<TopicTableItem, TableButtonsState> call(TableColumn<TopicTableItem, TableButtonsState> p) {
						return new TopicTableButtonCell(TopicTable.this);
					}

				});


		buttonsCol.setCellValueFactory(new PropertyValueFactory<TopicTableItem, TableButtonsState>("tableButtonsState"));
		topicCol.setCellValueFactory(new PropertyValueFactory<TopicTableItem, String>("topic"));
		valueCol.setCellValueFactory(new PropertyValueFactory<TopicTableItem, String>("value"));
		hostCol.setCellValueFactory(new PropertyValueFactory<TopicTableItem, String>("from"));
		timeCol.setCellValueFactory(new PropertyValueFactory<TopicTableItem, String>("time"));

		table.getColumns().addAll(buttonsCol, topicCol, valueCol, hostCol, timeCol);


		//localData = FXCollections.observableArrayList( data );
		localData = FXCollections.observableArrayList();
		table.setItems(localData);

		//ObservableList<TopicTableItem> 
		//updateLocalData(data);

		data.addListener(new ListChangeListener<TopicItem>(){

			@Override
			//public void onChanged(javafx.collections.ListChangeListener.Change<? extends TopicItem> c) {
			public void onChanged(Change<? extends TopicItem> c) {
				if( topicListUpdateEnabled )
				{
					//localData = FXCollections.observableArrayList( data );
					//table.setItems( localData );

					//table.setItems( FXCollections.observableArrayList( data ) );

					updateLocalData(data);
				}
			}

		});


		openWindow();
	}

	public void updateLocalData(ObservableList<TopicItem> data) 
	{
		data.forEach( ti -> {

			final AtomicReference<Boolean> have = new AtomicReference<Boolean>(false);  
			synchronized (localData) {
				for( int i = 0; i < localData.size(); i++)
				{
					TopicTableItem lti = localData.get(i);
					if( lti.sameHostAndTopic(ti) )
					{
						lti.assignFrom(ti);
						have.set(true);
						localData.set(i, lti); // to fire table update

					}
				}
			}


			if( !have.get() )
			{
				TopicTableItem nti = new TopicTableItem(ti);
				localData.add(nti);
			}
		});

		//table.refresh();
	}

	private static final Image windowIcon = ImageUtils.getImage("content256.png");

	private void openWindow() {

		VBox vbox = new VBox(makeToolBar(),table);
		vbox.setFillWidth(true);

		VBox.setVgrow(table, Priority.ALWAYS);

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
				result.ifPresent(newTopicName -> { 
					TopicTableItem nti = new TopicTableItem( mqtt_udp_defs.PTYPE_PUBLISH, newTopicName );
					localData.add(nti);
				});
			} // TODO
		});


		Button searchButton = new Button();
		searchButton.setTooltip(new Tooltip("Search for topic"));
		searchButton.setGraphic(ImageUtils.getIcon32("analysis"));
		searchButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {  
				Optional<String> result = searchTopicDialog();
				//result.ifPresent(newTopicName -> { System.out.println(newTopicName);	});
				if( result.isPresent() )
					search(result.get());
			} // TODO
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
			public void handle(ActionEvent event) { autoNetworkSendEnabled = refreshValueButton.isSelected(); }
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


	private final TextInputDialog ntd = new TextInputDialog();

	{
		Stage stage = (Stage) ntd.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ImageUtils.getImage("tests256.png"));
		ntd.setTitle("Create new topic");
		ntd.setHeaderText("New topic name");
	}

	private Optional<String> newTopicDialog()
	{
		//String ret = null;

		//td.setContentText("");
		//td.setGraphic(newTopicIcon);
		ntd.setGraphic(ImageUtils.getIcon32("tests"));
		//td.getDialogPane().setGraphic(newTopicIcon);

		return ntd.showAndWait();
	}


	private final TextInputDialog std = new TextInputDialog();

	{
		Stage stage = (Stage) std.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ImageUtils.getImage("analysis256.png"));
		std.setTitle("Search for topic");
		std.setHeaderText("Topic to find");
		std.setGraphic(ImageUtils.getIcon32("analysis"));
	}

	private Optional<String> searchTopicDialog()
	{	
		return std.showAndWait();
	}


	private void search(String findmeIn)
	{
		ObservableList<TableColumn<TopicTableItem, ?>> cols = table.getColumns();
		TableColumn<TopicTableItem, ?> col = cols.get(1);

		String findme = findmeIn.toLowerCase();
		//System.out.println("Look for "+findme);

		TableViewSelectionModel<TopicTableItem> sel = table.getSelectionModel();
		sel.setSelectionMode(SelectionMode.MULTIPLE);
		sel.clearSelection();

		for(int i=0; i< localData.size(); i++) 
		{
			String cellValue = col.getCellData(localData.get(i)).toString().toLowerCase();
			//System.out.println("Have "+cellValue);

			if(cellValue.contains(findme)) 
			{
				sel.select(i);
				//System.out.println("Select "+cellValue);
			}
		}		

		table.setSelectionModel(sel);

	}

	/**
	 * Find in items list item by index, send to net 
	 */
	public void sendRecord(int index, boolean limitSendToHost) 
	{
		TopicTableItem it = localData.get(index);

		try {
			if(limitSendToHost)
			{
				InetAddress addr = InetAddress.getByName(it.getFrom());
				//System.out.println("Send "+it+" to "+addr);
				it.sendTo( addr );
			} else
			{
				//System.out.println("Send "+it+" to all");
				it.sendToAll();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * Find in items list item by index, send SUBSCRIBE with topic of the record 
	 */
	public void sendRequest(int index, boolean limitSendToHost) 
	{
		TopicTableItem it = localData.get(index);
		String topic = it.getTopic();
		SubscribePacket sp = new SubscribePacket(topic);
		
		try {
			if(limitSendToHost)
			{
				InetAddress addr = InetAddress.getByName(it.getFrom());
				//System.out.println("Send SUBSCRIBE "+it+" to "+addr);
				sp.send( addr );
			} else
			{
				//System.out.println("Send SUBSCRIBE "+it+" to all");
				sp.send();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	
	


}
