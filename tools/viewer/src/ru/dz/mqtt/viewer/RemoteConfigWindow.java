package ru.dz.mqtt.viewer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

	private final Map<ConfigurableHost,RemoteConfigTab> tabs = new HashMap<ConfigurableHost, RemoteConfigTab>();

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
		sendAllButton.setOnAction( e -> currentTab().ifPresent( t->t.sendAll() )  );
		sendAllButton.setDisable(true);


		Button requestAllButton = new Button();
		requestAllButton.setTooltip(new Tooltip("Request all settings"));
		requestAllButton.setGraphic(ImageUtils.getIcon32("order"));
		requestAllButton.setOnAction( e -> currentTab().ifPresent( t->t.requestAll() ) );
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



	public Optional<RemoteConfigTab> currentTab() {
		Tab tab = tabPane.getSelectionModel().getSelectedItem();
		if (tab instanceof RemoteConfigTab) 
			return Optional.ofNullable( (RemoteConfigTab)tab );					
		return Optional.empty();
	}


	private void createTab(ConfigurableHost ch) 
	{
		RemoteConfigTab oldTab = tabs.get(ch);
		if( oldTab != null )
		{
			oldTab.updateFromHost(ch);
			return;
		}

		RemoteConfigTab tab = new RemoteConfigTab(ch,this);

		Platform.runLater( new Runnable() {
			@Override
			public void run() { tabPane.getTabs().add(tab); }
		} );

		tabs.put(ch, tab);
	}


	private void addParameter(ConfigurableParameter cp) 
	{
		ConfigurableHost ch = cp.getConfigurableHost();

		RemoteConfigTab tab = tabs.get(ch);

		if( tab == null )
		{
			System.err.println("no tab");
			return;
		}

		tab.addParameter( cp );

	}


	public boolean isAutoSend() { return autoSend;	}


}
