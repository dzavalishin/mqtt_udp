package ru.dz.mqtt.viewer;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.dz.mqtt_udp.PacketSourceMultiServer;
import ru.dz.mqtt_udp.config.ConfigurableHost;
import ru.dz.mqtt_udp.config.ConfigurableParameter;
import ru.dz.mqtt_udp.config.Controller;
import ru.dz.mqtt_udp.util.image.ImageUtils;

public class RemoteConfigWindow {

	private Controller rc;
	
	private Stage rcWindow = new Stage();

	/** Automatically send on enter in edit field */
	private boolean autoSend = false;

	
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

	public void setVisible(boolean is)
	{
		if( is ) rcWindow.show();
		else rcWindow.hide();
	}

	public boolean isVisible()
	{
		return rcWindow.isShowing();
	}
	

	private TabPane tabPane = new TabPane();

	//private static final Image windowIcon = ImageUtils.getImage("content256.png");
	private static final Image windowIcon = ImageUtils.getImage("surveys256.png");

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
	private static final ImageView sendAllIcon = ImageUtils.getIcon32("options");

	private HBox makeToolBar()
	{

		Button sendAllButton = new Button();
		sendAllButton.setTooltip(new Tooltip("Send all settings"));
		sendAllButton.setGraphic(sendAllIcon);
		sendAllButton.setOnAction( e -> {} );
		sendAllButton.setDisable(true);


		Button requestAllButton = new Button();
		requestAllButton.setTooltip(new Tooltip("Request all settings"));
		requestAllButton.setGraphic(ImageUtils.getIcon32("order"));
		requestAllButton.setOnAction( e -> {} );
		requestAllButton.setDisable(true);

		ToggleButton refreshListButton = new ToggleButton();
		refreshListButton.setTooltip(new Tooltip("Enable automatic value refresh"));
		refreshListButton.setGraphic(unlockedIcon);
		refreshListButton.setSelected(true);
		refreshListButton.setOnAction( e -> {} );
		refreshListButton.setDisable(true);

		ToggleButton refreshValueButton = new ToggleButton();
		refreshValueButton.setTooltip(new Tooltip("Enable automatic value send"));
		refreshValueButton.setGraphic(ImageUtils.getIcon32("refresh"));
		refreshValueButton.setOnAction( e -> autoSend = refreshValueButton.isSelected() );


		ToolBar leftTb = new ToolBar();
		leftTb.getItems().addAll(sendAllButton,requestAllButton);


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
		tab.setClosable(false);
		
		//VBox vbox = new VBox(new Rectangle(200,200, Color.LIGHTSALMON));
		VBox vbox = new VBox(8);

		vbox.setStyle("-fx-padding: 10;" + 
                "-fx-border-style: solid inside;" + 
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" + 
                //"-fx-border-radius: 5;" + 
                "-fx-border-color: lightgrey;");
		
		vbox.getChildren().add( new Label(
				"  MAC: "+ ch.getMacAddressString() + "   IP: "+ch.getIpAddressString()
				) );
		
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
		
		Label kindLabel = new Label(cp.getKind()+":");
		kindLabel.setPrefWidth(30);
		
		Label nameLabel = new Label(cp.getName() );
		nameLabel.setPrefWidth(50);
		nameLabel.setStyle("-fx-font-weight: bold;");
		
		TextField valueField = new TextField(cp.getValue());
		valueField.setOnAction( e -> { if(autoSend ) cp.sendNewValue( valueField.getText()); } );
		
		
		hbox.getChildren().add(kindLabel);
		hbox.getChildren().add(nameLabel);
		hbox.getChildren().add(valueField);

		makeButton( hbox, ImageUtils.getIcon("options"), "Send to network", e -> cp.sendNewValue( valueField.getText()) );
		makeButton( hbox, ImageUtils.getIcon("order"), "Request from network", e -> cp.requestAgain() );
		
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

	
	private void makeButton( HBox hb, ImageView icon, String toolTip, EventHandler<ActionEvent> handler )
	{
		final Button cellButton = new Button();
		
		cellButton.setGraphic(icon);
		cellButton.setTooltip(new Tooltip(toolTip));
		
		cellButton.setOnAction(handler);
		
		hb.getChildren().add(cellButton);
	}

	
}
