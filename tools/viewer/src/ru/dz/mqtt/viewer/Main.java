package ru.dz.mqtt.viewer;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import ru.dz.mqtt_udp.IPacket;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class Main extends Application {
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
			HBox log = makeLog();

			//content.setPrefWidth(800);
			//log.setPrefWidth(800);

			VBox vbox = new VBox(menu,content,log);
			AnchorPane pane = new AnchorPane(vbox);

			Scene scene = new Scene( pane );

			// setting the stage
			primaryStage.setScene( scene );
			primaryStage.setTitle( "MQTT/UDP Traffic Viewer" );
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
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
		listv.setPrefWidth(800);

		HBox hbox = new HBox(listv);

		return hbox;
	}

	private HBox makeContent() {
		ListView<ru.dz.mqtt.viewer.TopicItem> listv = makeListView();

		HBox hbox = new HBox(listv);

		return hbox;
	}

	private MenuBar makeMenu() {

		Menu fileMenu = new Menu("File");


		Menu serverMenu = new Menu("Server");
		MenuBar mb = new MenuBar(fileMenu,serverMenu);
		return mb;
	}

	public static void main(String[] args) {
		launch(args);
	}



	private ObservableList<TopicItem> listItems =FXCollections.observableArrayList();
	private void setListItem(TopicItem i)
	{
		//listItems.add(i);
		listItems.forEach(
				ti -> {
					if(ti.getTopic() == i.getTopic())
						listItems.remove(ti);
				}

				);
		
		listItems.add(0, i);
	}


	private ListView<TopicItem> makeListView() {
		ListView<TopicItem> lv = new ListView<TopicItem>();
		lv.setPrefWidth(800);

		//ObservableSet<TopicItem> items = FXCollections.emptyObservableSet();



		lv.setItems(listItems);

		MqttUdpDataSource ds = new MqttUdpDataSource();
		ds.setSink(ti -> { 
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					setListItem(ti); 
					addLogItem(ti); 
				}
			});


		});

		return lv;
	}




}
