package ru.dz.mqtt.viewer;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import ru.dz.mqtt_udp.items.AbstractItem;
import ru.dz.mqtt_udp.items.TopicItem;
import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;
import ru.dz.mqtt_udp.util.MqttUdpRuntimeException;
import ru.dz.mqtt_udp.util.mqtt_udp_defs;
import ru.dz.mqtt_udp.util.image.ImageUtils;


public class Main extends Application {
	private static final int TOOLBAR_HEIGHT = 34;
	private FileLogger flog = new FileLogger();
	private FileChooser fch = new FileChooser();

	volatile protected boolean updateEnabled = true;
	volatile protected boolean displayPings = false;

	private ObservableList<TopicItem> listItems = FXCollections.observableArrayList();
	private TopicTable generalTopicTable = new TopicTable(listItems);

	
	//private Stage stage;

	private SplitPane splitPane;
	private HBox hosts;

	private static final Image applicationIcon = ImageUtils.getImage("maintenance256.png");

	static private final Alert aboutAlert = new Alert(AlertType.INFORMATION);

	private static final int DEFAULT_WIDTH = 1000;

	private PingSender pingSender = new PingSender(); 


	@Override
	public void start(Stage primaryStage) {
		try {

			//stage = primaryStage;


			primaryStage.getIcons().add(applicationIcon);

			fch.setTitle("Event log file");
			fch.setInitialFileName("MQTT_UDP.log");		

			HBox menubars;
			{
				Region spacer = new Region();
				spacer.getStyleClass().add("menu-bar");
				HBox.setHgrow(spacer, Priority.SOMETIMES);

				MenuBar leftMenu = makeLeftMenu();
				ToolBar toolBar = makeToolBar();				
				MenuBar rightMenu = makeRightMenu();

				//leftMenu.setPrefHeight(toolBar.getHeight());
				//rightMenu.setPrefHeight(toolBar.getHeight());

				leftMenu.setPrefHeight(TOOLBAR_HEIGHT);
				rightMenu.setPrefHeight(TOOLBAR_HEIGHT);
				toolBar.setPrefHeight(TOOLBAR_HEIGHT);

				toolBarUpdateButton.setPrefHeight(12);

				menubars = new HBox(leftMenu, spacer, toolBar, rightMenu);			
				//menubars = new HBox(makeLeftMenu(), spacer, makeToolBar(), makeRightMenu());							
				//menubars.setMinHeight(32);
			}

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

			aboutAlert.setTitle("About MQTT/UDP viewer");
			aboutAlert.setHeaderText(String.format( "MQTT/UDP viewer version %d.%d",
					mqtt_udp_defs.PACKAGE_VERSION_MAJOR, mqtt_udp_defs.PACKAGE_VERSION_MINOR ) );
			aboutAlert.setContentText("Network is broker!");
			aboutAlert.initOwner(primaryStage);

			initNetwork();


			//toolBarSendButton.setSelected(true);
			//generalTopicTable.setVisible(true);
			// TODO button is not unselected on manual window close
			ObjectProperty<EventHandler<WindowEvent>> ohp = generalTopicTable.onHiddenProperty();
			ohp.addListener( e -> { toolBarSendButton.setSelected( generalTopicTable.isVisible() ); });
			
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


	private ListView<AbstractItem> logListView = new ListView<AbstractItem>();
	private ObservableList<AbstractItem> logItems = FXCollections.observableArrayList ();
	private void addLogItem(AbstractItem i)
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
		ListView<TopicItem> listv = makeListView();

		HBox hbox = new HBox(listv);
		hbox.setFillHeight(true);

		return hbox;
	}


	private CheckMenuItem updateMenuItem = new CheckMenuItem("Update");	
	//private CheckMenuItem updateTopMenuItem = new CheckMenuItem();
	private ToggleButton toolBarUpdateButton = new ToggleButton();
	private ToggleButton toolBarSendButton = new ToggleButton();


	private MenuBar makeLeftMenu() {

		// ---------- File ----------------------------------------

		Menu fileMenu = new Menu("File");

		MenuItem logStart = new MenuItem("Start log");
		MenuItem logStop = new MenuItem("Stop log");

		MenuItem exitMenuItem = new MenuItem("Exit");

		fileMenu.getItems().addAll( logStart, logStop, new SeparatorMenuItem(), exitMenuItem );


		// ---------- Display -------------------------------------
		Menu displayMenu = new Menu("Display");
		//displayMenu.addEventHandler(eventType, eventHandler);

		CheckMenuItem viewHostsMenuItem = new CheckMenuItem("Hosts view");

		CheckMenuItem showPingsMenuItem = new CheckMenuItem("Display Ping/Responce");

		displayMenu.getItems().addAll(updateMenuItem, new SeparatorMenuItem(), viewHostsMenuItem, showPingsMenuItem);

		// ---------- Send ----------------------------------------
		
		Menu sendMenu = new Menu("Send");

		CheckMenuItem replyMenuItem = new CheckMenuItem("Replies");
		replyMenuItem.setSelected(pingSender.isEnabled());
		replyMenuItem.setOnAction( new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { ds.setMuted(!replyMenuItem.isSelected()); } 
		});
		
		
		CheckMenuItem pingMenuItem = new CheckMenuItem("Ping");
		pingMenuItem.setSelected(pingSender.isEnabled());
		pingMenuItem.setOnAction( new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { pingSender.setEnabled(pingMenuItem.isSelected()); } 
		});
		
		
		sendMenu.getItems().addAll(replyMenuItem, pingMenuItem);
		
		// ---------- Bar -----------------------------------------
		
		MenuBar mb = new MenuBar(fileMenu,displayMenu,sendMenu);


		showPingsMenuItem.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				displayPings = showPingsMenuItem.isSelected();
			}
		});
		showPingsMenuItem.setSelected(false);
		displayPings = false;
		
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
				//updateEnabled = updateMenuItem.isSelected();
				switchRunStop();
			}
		});


		exitMenuItem.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				//Platform.exit(); System.exit(0);				
			}
		});

		logStart.setGraphic(ImageUtils.getIcon("Folder-Add"));
		logStart.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
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
						//e.printStackTrace();
						GlobalErrorHandler.handleError(ErrorType.IO, e);
					}				
				}
			}
		});

		logStop.setGraphic(ImageUtils.getIcon("Folder-Delete"));
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

	private static final ImageView runIcon = ImageUtils.getIcon("start");
	private static final ImageView stopIcon = ImageUtils.getIcon("pause");
	private ToolBar makeToolBar()
	{
		toolBarUpdateButton.setTooltip(new Tooltip("Press F5 to stop/run"));
		toolBarUpdateButton.setGraphic(runIcon);
		toolBarUpdateButton.setSelected(true);
		toolBarUpdateButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { switchRunStop(); }
		});

		//toolBarUpdateButton.setMaxHeight(12);
		//toolBarUpdateButton.setMaxWidth(12);
		
		toolBarSendButton.setTooltip(new Tooltip("Open data send interface"));
		toolBarSendButton.setGraphic(ImageUtils.getIcon("content"));
		toolBarSendButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { generalTopicTable.setVisible(toolBarSendButton.isSelected()); }
		});
		
		ToolBar tb = new ToolBar();
		//tb.setMaxHeight(16);
		tb.getItems().addAll(toolBarSendButton,toolBarUpdateButton);

		return tb;
	}

	private void switchRunStop()
	{
		//updateEnabled = updateMenuItem.isSelected();
		updateEnabled = !updateEnabled;
		updateMenuItem.setSelected(updateEnabled);
		toolBarUpdateButton.setSelected(updateEnabled);

		toolBarUpdateButton.setGraphic( updateEnabled ? runIcon : stopIcon );
		
		/* not needed as we do not occupy udp port exclusively
		if( updateEnabled ) ds.requestStart();
		else				ds.requestStop();
		*/
	}

	private void showAboutAlert() {
		aboutAlert.showAndWait();
	}


	public static void main(String[] args) {
		launch(args);
	}



	private ListView<TopicItem> topicListView = new ListView<TopicItem>();
	private void setListItem(AbstractItem item)
	{
		if(!item.typeWithTopic()) return;
		
		if(! (item instanceof TopicItem) ) 
			return;
		
		TopicItem ti = (TopicItem) item;
			
		// Dumb code, sorry
		int nItems = listItems.size();
		for( int j = 0; j < nItems; j++ )
		{
			TopicItem ci = listItems.get(j);
			if(ci.getTopic().equals(ti.getTopic()) )
			{
				listItems.remove(j);
				listItems.add(j, ti);

				MultipleSelectionModel<TopicItem> sm = topicListView.getSelectionModel();
				if(sm.isEmpty())
					sm.select(j);

				return;
			}
		}

		listItems.add(0, ti);

		MultipleSelectionModel<TopicItem> sm = topicListView.getSelectionModel();
		if(sm.isEmpty())
			sm.select(0);
	}


	private ListView<TopicItem> makeListView() {
		topicListView.setPrefWidth(DEFAULT_WIDTH);

		//ObservableSet<TopicItem> items = FXCollections.emptyObservableSet();
		topicListView.setItems(listItems);

		topicListView.setCellFactory(new Callback<ListView<TopicItem>, ListCell<TopicItem>>() {
			@Override
			public ListCell<TopicItem> call(ListView<TopicItem> arg0) {

				//final Label leadLbl = new Label();
	            final Tooltip tooltip = new Tooltip();
	            final ListCell<TopicItem> cell = new ListCell<TopicItem>() {
	              @Override
	              public void updateItem(TopicItem item, boolean empty) {
	                super.updateItem(item, empty);
	                if (item != null) {
	                  //leadLbl.setText(item.toString());
	                  setText(item.toString());
	                  tooltip.setText("From "+item.getFrom());
	                  setTooltip(tooltip);
	                }
	              }
	            }; // ListCell				

				return cell;
			}
			
		});
	/* */
		
		
		
		return topicListView;
	}



	private MqttUdpDataSource ds;
	private void initNetwork()
	{
		try {
			ds = new MqttUdpDataSource();
		} catch (SocketException e) {
			throw new MqttUdpRuntimeException(e);
		}
		ds.setSink(ti -> { 
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					if( updateEnabled )
					{
						if( displayPings || (!ti.isPingOrResponce()) )
						{
							setListItem(ti); 
							addLogItem(ti);
						}
						//addHostItem( new HostItem(ti.getFrom()) );
						addHostItem( new HostItem(ti) );

						flog.logItem(ti);
					}
				}
			});


		});

	}


}
