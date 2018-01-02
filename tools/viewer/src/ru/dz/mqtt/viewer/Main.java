package ru.dz.mqtt.viewer;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.stage.Stage;
import ru.dz.mqtt_udp.IPacket;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class Main extends Application {
	private static final int DEFAULT_WIDTH = 1000;
	@Override
	public void start(Stage primaryStage) {
		try {

			//BorderPane root = new BorderPane();
			//Scene scene = new Scene(root,400,400);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			//primaryStage.setScene(scene);
			//primaryStage.show();

			// constructing our scene
			//URL url = getClass().getResource("TopicTree.fxml");
			//AnchorPane pane = FXMLLoader.load( url );

			MenuBar menu = makeMenu();

			HBox content = makeContent();
			content.setFillHeight(true);
			HBox log = makeLog();
			log.setFillHeight(true);
			HBox hosts = makeHosts();
			hosts.setFillHeight(true);

			/*
			VBox vbox = new VBox(menu,content,log);
			vbox.setFillWidth(true);
			AnchorPane pane = new AnchorPane(vbox);
			Scene scene = new Scene( pane );
			*/
			
			SplitPane sp = new SplitPane(content,log,hosts);
			sp.setOrientation(Orientation.VERTICAL);
			sp.setDividerPositions(0.6f,0.8f);
			SplitPane.setResizableWithParent(content, true);
			SplitPane.setResizableWithParent(log, true);
			SplitPane.setResizableWithParent(hosts, true);
			
			VBox vbox = new VBox(menu,sp);
			vbox.setFillWidth(true);
			
			Scene scene = new Scene( vbox );

			// setting the stage
			primaryStage.setScene( scene );
			primaryStage.setTitle( "MQTT/UDP Traffic Viewer" );			
			primaryStage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0);} );
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

	
	private ObservableList<TopicItem> logItems = FXCollections.observableArrayList ();
	private void addLogItem(TopicItem i)
	{
		logItems.add(i);
		if( logItems.size() > 400 )
			logItems.remove(0);
	}
	
	private HBox makeLog() {
		ListView<TopicItem> listv = new ListView<TopicItem>();
		listv.setItems(logItems);
		listv.setPrefWidth(DEFAULT_WIDTH);

		HBox hbox = new HBox(listv);

		return hbox;
	}

	private HBox makeContent() {
		ListView<ru.dz.mqtt.viewer.TopicItem> listv = makeListView();

		HBox hbox = new HBox(listv);
		hbox.setFillHeight(true);

		return hbox;
	}

	private MenuBar makeMenu() {

		Menu fileMenu = new Menu("File");


		CheckMenuItem updateMenuItem = new CheckMenuItem("Update");
		

		Menu displayMenu = new Menu("Display");
		//displayMenu.addEventHandler(eventType, eventHandler);

		
		MenuBar mb = new MenuBar(fileMenu,displayMenu);
		return mb;
	}

	public static void main(String[] args) {
		launch(args);
	}



	private ListView<TopicItem> topicListView = new ListView<TopicItem>();
	private ObservableList<TopicItem> listItems =FXCollections.observableArrayList();
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

		MqttUdpDataSource ds = new MqttUdpDataSource();
		ds.setSink(ti -> { 
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					setListItem(ti); 
					addLogItem(ti);
					addHostItem( new HostItem(ti.getFrom()));
				}
			});


		});

		return topicListView;
	}




}
