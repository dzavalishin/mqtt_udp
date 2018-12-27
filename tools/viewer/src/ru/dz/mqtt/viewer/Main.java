package ru.dz.mqtt.viewer;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ru.dz.mqtt_udp.IPacket;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


public class Main extends Application {
	private FileLogger flog = new FileLogger();
	FileChooser fch = new FileChooser();

	//private Stage stage;

	private SplitPane splitPane;
	private HBox hosts;


	private static final int DEFAULT_WIDTH = 1000;
	@Override
	public void start(Stage primaryStage) {
		try {

			//stage = primaryStage;

			//BorderPane root = new BorderPane();
			//Scene scene = new Scene(root,400,400);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			//primaryStage.setScene(scene);
			//primaryStage.show();

			// constructing our scene
			//URL url = getClass().getResource("TopicTree.fxml");
			//AnchorPane pane = FXMLLoader.load( url );

			fch.setTitle("Event log file");
			fch.setInitialFileName("MQTT_UDP.log");
			
			MenuBar leftMenu = makeLeftMenu();

			Region spacer = new Region();
	        spacer.getStyleClass().add("menu-bar");
	        HBox.setHgrow(spacer, Priority.SOMETIMES);
	        HBox menubars = new HBox(leftMenu, spacer, makeRightMenu());			
			
			HBox content = makeContent();
			content.setFillHeight(true);
			HBox log = makeLog();
			log.setFillHeight(true);
			//HBox hosts = makeHosts();
			hosts = makeHosts();
			hosts.setFillHeight(true);

			/*
			VBox vbox = new VBox(menu,content,log);
			vbox.setFillWidth(true);
			AnchorPane pane = new AnchorPane(vbox);
			Scene scene = new Scene( pane );
			 */

			//SplitPane 
			splitPane = new SplitPane(content,log,hosts);
			splitPane.setOrientation(Orientation.VERTICAL);
			splitPane.setDividerPositions(0.5f,0.8f);
			SplitPane.setResizableWithParent(content, true);
			SplitPane.setResizableWithParent(log, true);
			SplitPane.setResizableWithParent(hosts, true);

			VBox vbox = new VBox(menubars,splitPane);
			vbox.setFillWidth(true);

			Scene scene = new Scene( vbox );

			Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
			//vbox.setMaxHeight(screenSize.getHeight()-20);
			
			// setting the stage
			primaryStage.setScene( scene );
			primaryStage.setTitle( "MQTT/UDP Traffic Viewer" );			
			primaryStage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0);} );
			//primaryStage.sizeToScene();
			primaryStage.setHeight( screenSize.getHeight() - 100 ); // TODO hack, redo
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private ListView<HostItem> hostListView = new ListView<HostItem>();
	private ObservableList<HostItem> hostItems = FXCollections.observableArrayList ();
	private void addHostItem(HostItem item)
	{
		// Dumb code, sorry
		/*
		hostItems.add(i);
		if( hostItems.size() > 400 )
			hostItems.remove(0);
		 */

		int nItems = hostItems.size();
		for( int j = 0; j < nItems; j++ )
		{
			HostItem ci = hostItems.get(j);
			if(ci.getHostName().equals(item.getHostName()) )
			{
				hostItems.remove(j);
				hostItems.add(j, item);

				MultipleSelectionModel<HostItem> sm = hostListView.getSelectionModel();
				if(sm.isEmpty())
					sm.select(j);

				return;
			}
		}

		hostItems.add(0, item);

	}

	private HBox makeHosts() {
		hostListView.setItems(hostItems);
		hostListView.setPrefWidth(DEFAULT_WIDTH);

		HBox hbox = new HBox(hostListView);

		return hbox;
	}


	private ListView<TopicItem> logListView = new ListView<TopicItem>();
	private ObservableList<TopicItem> logItems = FXCollections.observableArrayList ();
	private void addLogItem(TopicItem i)
	{
		logItems.add(0,i);
		if( logItems.size() > 400 )
			logItems.remove(logItems.size()-1);
	}

	private HBox makeLog() {
		logListView.setItems(logItems);
		logListView.setPrefWidth(DEFAULT_WIDTH);

		HBox hbox = new HBox(logListView);

		return hbox;
	}

	private HBox makeContent() {
		ListView<ru.dz.mqtt.viewer.TopicItem> listv = makeListView();

		HBox hbox = new HBox(listv);
		hbox.setFillHeight(true);

		return hbox;
	}

	private MenuBar makeLeftMenu() {

		Menu fileMenu = new Menu("File");

		MenuItem logStart = new MenuItem("Start log");
		MenuItem logStop = new MenuItem("Stop log");

		MenuItem exitMenuItem = new MenuItem("Exit");

		fileMenu.getItems().addAll( logStart, logStop, new SeparatorMenuItem(), exitMenuItem );
		// log open dialog crashes :(
		//fileMenu.getItems().addAll( exit );


		Menu displayMenu = new Menu("Display");
		//displayMenu.addEventHandler(eventType, eventHandler);

		CheckMenuItem updateMenuItem = new CheckMenuItem("Update");	
		CheckMenuItem viewHostsMenuItem = new CheckMenuItem("Hosts view");

		displayMenu.getItems().addAll(updateMenuItem, new SeparatorMenuItem(), viewHostsMenuItem);

		MenuBar mb = new MenuBar(fileMenu,displayMenu);


		viewHostsMenuItem.setSelected(true);
		viewHostsMenuItem.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				boolean on = viewHostsMenuItem.isSelected();
				hosts.setVisible(on);
				hosts.setFillHeight(on);
				//if(on)		hosts.autosize();
				//else		hosts.setPrefHeight(0);
				splitPane.autosize();
			}
		});


		updateMenuItem.setAccelerator(KeyCombination.keyCombination("F5"));
		updateMenuItem.setSelected(true);
		updateMenuItem.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				updateEnabled = updateMenuItem.isSelected();				
			}
		});


		exitMenuItem.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				//Platform.exit(); System.exit(0);				
			}
		});


		logStart.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
		logStart.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				//File fname = fch.showOpenDialog(stage);
				//File fname = fch.showOpenDialog(logStart.getParentPopup().getScene().getWindow());
				File fname = fch.showSaveDialog(logStart.getParentPopup().getScene().getWindow());
				if( fname != null )
				{
					try {
						flog.startLog(fname);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				}
			}
		});

		logStop.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				flog.stopLog();				
			}
		});

		return mb;
	}

	
	private MenuBar makeRightMenu() {
		Menu helpMenu = new Menu("Help");

		MenuItem help = new MenuItem("Help");
		MenuItem goSite = new MenuItem("Visit project on GitHub");

		MenuItem aboutMenuItem = new MenuItem("About");

		helpMenu.getItems().addAll( help, goSite, new SeparatorMenuItem(), aboutMenuItem );

		
		help.setAccelerator(KeyCombination.keyCombination("F1"));
		help.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				getHostServices().showDocument("https://github.com/dzavalishin/mqtt_udp/wiki/MQTT-UDP-Viewer-Help");			
				}
		});
		
		goSite.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				getHostServices().showDocument("https://github.com/dzavalishin/mqtt_udp");			
				}
		});

		aboutMenuItem.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { showAboutAlert(); }
		});
		
		MenuBar mb = new MenuBar(helpMenu);
		return mb;
	}
	
	 private void showAboutAlert() {
	        Alert alert = new Alert(AlertType.INFORMATION);
	        alert.setTitle("About MQTT/UDP viewer");
	        alert.setHeaderText("MQTT/UDP viewer version 1.0");
	        alert.setContentText("Network is broker!");
	 
	        alert.showAndWait();
	    }
	
	
	public static void main(String[] args) {
		launch(args);
	}



	private ListView<TopicItem> topicListView = new ListView<TopicItem>();
	private ObservableList<TopicItem> listItems =FXCollections.observableArrayList();
	protected boolean updateEnabled = true;
	private void setListItem(TopicItem item)
	{
		/*
		//listItems.add(i);
		listItems.forEach(
				ti -> {
					if(ti.getTopic() == item.getTopic())
						listItems.remove(ti);
				}

				);
		 */
		// Dumb code, sorry
		int nItems = listItems.size();
		for( int j = 0; j < nItems; j++ )
		{
			TopicItem ci = listItems.get(j);
			if(ci.getTopic().equals(item.getTopic()) )
			{
				listItems.remove(j);
				listItems.add(j, item);

				MultipleSelectionModel<TopicItem> sm = topicListView.getSelectionModel();
				if(sm.isEmpty())
					sm.select(j);

				return;
			}
		}

		listItems.add(0, item);

		MultipleSelectionModel<TopicItem> sm = topicListView.getSelectionModel();
		if(sm.isEmpty())
			sm.select(0);
	}


	private ListView<TopicItem> makeListView() {
		topicListView.setPrefWidth(DEFAULT_WIDTH);

		//ObservableSet<TopicItem> items = FXCollections.emptyObservableSet();



		topicListView.setItems(listItems);

		MqttUdpDataSource ds;
		try {
			ds = new MqttUdpDataSource();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		ds.setSink(ti -> { 
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					if( updateEnabled )
					{
						setListItem(ti); 
						addLogItem(ti);
						addHostItem( new HostItem(ti.getFrom()) );
						
						flog.logItem(ti);
					}
				}
			});


		});

		return topicListView;
	}




}
