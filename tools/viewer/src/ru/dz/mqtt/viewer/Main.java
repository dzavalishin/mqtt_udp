package ru.dz.mqtt.viewer;
	
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
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
			VBox vbox = new VBox(menu,content);
			AnchorPane pane = new AnchorPane(vbox);
			
		    Scene scene = new Scene( pane );
		    
		    // setting the stage
		    primaryStage.setScene( scene );
		    primaryStage.setTitle( "Hello World Demo" );
		    primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
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


	private ListView<TopicItem> makeListView() {
		ListView<TopicItem> lv = new ListView<TopicItem>();
		
		return lv;
	}




}
