package ru.dz.mqtt.viewer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.PacketSourceMultiServer;
import ru.dz.mqtt_udp.config.ConfigurableHost;
import ru.dz.mqtt_udp.config.ConfigurableParameter;
import ru.dz.mqtt_udp.config.Controller;
import ru.dz.mqtt_udp.util.image.ImageUtils;

public class RemoteConfigWindow {

	private Controller rc;
	
	private Stage rcWindow = new Stage();

	public RemoteConfigWindow() 
	{
		PacketSourceMultiServer ms = new PacketSourceMultiServer();
		
		rc = new Controller(ms);
		rc.setNewHostListener( ch -> createTab(ch) ); 
		rc.setNewParameterListener( cp -> addParameter( cp ) );
		
		openWindow();
		
		ms.requestStart();
		rc.requestStart();
	}


	private TabPane tabPane = new TabPane();

	private static final Image windowIcon = ImageUtils.getImage("content256.png");

	private void openWindow() {

		/*
		{
		Tab tab = new Tab();
		tab.setText("new tab 1");
		tab.setContent(new Rectangle(200,200, Color.LIGHTSTEELBLUE));
		tabPane.getTabs().add(tab);		
		}
		{
		Tab tab = new Tab();
		tab.setText("new tab 2");
		tab.setContent(new Rectangle(200,200, Color.LIGHTGOLDENRODYELLOW));
		tabPane.getTabs().add(tab);		
		}*/
		
		VBox vbox = new VBox(makeToolBar(),tabPane);
		vbox.setFillWidth(true);

		VBox.setVgrow(tabPane, Priority.ALWAYS);

		Scene rcScene = new Scene(vbox, 800, 500);

		// New window (Stage)
		rcWindow.setTitle("Remote Config");
		rcWindow.setScene(rcScene);


		rcWindow.setMinWidth(500);

		rcWindow.getIcons().add(windowIcon);

		// TODO remove
		rcWindow.show();

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
				// TODO
			} 
		});


		Button searchButton = new Button();
		searchButton.setTooltip(new Tooltip("Search for topic"));
		searchButton.setGraphic(ImageUtils.getIcon32("analysis"));
		searchButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				// TODO
			} 
		});

		ToggleButton refreshListButton = new ToggleButton();
		refreshListButton.setTooltip(new Tooltip("Enable automatic topic list refresh"));
		refreshListButton.setGraphic(unlockedIcon);
		refreshListButton.setSelected(true);
		refreshListButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				// TODO
			} 
		});

		ToggleButton refreshValueButton = new ToggleButton();
		refreshValueButton.setTooltip(new Tooltip("Enable automatic value send"));
		refreshValueButton.setGraphic(ImageUtils.getIcon32("refresh"));
		refreshValueButton.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) { /* TODO */ }
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


	private Map<ConfigurableHost,Tab> tabs = new HashMap<ConfigurableHost, Tab>();
	private void createTab(ConfigurableHost ch) 
	{
		Tab oldTab = tabs.get(ch);
		if( oldTab != null )
		{
			// TODO update tab
		}
		
		Tab tab = new Tab();
		tab.setText(ch.getMacAddressString());
		
		//VBox vbox = new VBox(new Rectangle(200,200, Color.LIGHTSALMON));
		VBox vbox = new VBox(8);

		vbox.setStyle("-fx-padding: 10;" + 
                "-fx-border-style: solid inside;" + 
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" + 
                //"-fx-border-radius: 5;" + 
                "-fx-border-color: lightgrey;");
		
		tab.setContent( vbox );
						
		Platform.runLater( new Runnable() {
			@Override
			public void run() { tabPane.getTabs().add(tab); }
			} );
		
		tabs.put(ch, tab);
	}

	private Map<ConfigurableParameter,HBox> controls = new HashMap<ConfigurableParameter, HBox>();
	private void addParameter(ConfigurableParameter cp) 
	{
		ConfigurableHost ch = cp.getConfigurableHost();

		Tab tab = tabs.get(ch);
		
		if( tab == null )
		{
			System.err.println("no tab");
			return;
		}
		
		VBox vbox = (VBox) tab.getContent();
		
		HBox hbox = controls.get(cp);
		if( hbox == null )
			hbox = new HBox(20);
		
		Label kindLabel = new Label(cp.getKind() );
		kindLabel.setPrefWidth(50);
		
		Label nameLabel = new Label(cp.getName() );
		nameLabel.setPrefWidth(50);
		
		TextField valueField = new TextField(cp.getValue());
		
		
		hbox.getChildren().add(kindLabel);
		hbox.getChildren().add(nameLabel);
		hbox.getChildren().add(valueField);

		final HBox addHbox = hbox;
		//ScrollPane scroll = new ScrollPane();
		//scroll.setContent(hbox);
		
		Platform.runLater( new Runnable() {
			@Override
			public void run() { 
				//vbox.getChildren().add(scroll); 
				vbox.getChildren().add(addHbox); 
				}
			} );

		
	}

	
}
