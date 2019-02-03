package ru.dz.mqtt.viewer;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import ru.dz.mqtt_udp.config.ConfigurableHost;
import ru.dz.mqtt_udp.config.ConfigurableParameter;
import ru.dz.mqtt_udp.util.image.ImageUtils;

public class RemoteConfigTab extends Tab 
{
	private RemoteConfigWindow rcw;
	private ConfigurableHost ch;
	private Map<ConfigurableParameter,HBox> controls = new HashMap<ConfigurableParameter, HBox>();

	public RemoteConfigTab(ConfigurableHost ch, RemoteConfigWindow rcw) {
		this.ch = ch;
		this.rcw = rcw;
		
		setText(ch.getMacAddressString());
		setClosable(false);
		
		VBox vbox = new VBox(8);

		vbox.setStyle("-fx-padding: 10;" + 
                "-fx-border-style: solid inside;" + 
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" + 
                //"-fx-border-radius: 5;" + 
                "-fx-border-color: lightgrey;");
		
		vbox.getChildren().add( new Label(
				getTabDescription(ch)
				) );
		
		setContent( vbox );
	}

	public String getTabDescription(ConfigurableHost ch) {
		return "  MAC: "+ ch.getMacAddressString() + "   IP: "+ch.getIpAddressString();
	}

	public void updateFromHost(ConfigurableHost newCh) {
		// TODO update tab		
	}

	public void addParameter(ConfigurableParameter cp) {
		VBox vbox = (VBox) getContent();
		
		HBox hbox = controls.get(cp);
		if( hbox == null )
			hbox = new HBox(20);
		
		Label kindLabel = new Label(cp.getKind()+":");
		kindLabel.setPrefWidth(30);
		
		Label nameLabel = new Label(cp.getName() );
		nameLabel.setPrefWidth(50);
		nameLabel.setStyle("-fx-font-weight: bold;");
		
		TextField valueField = new TextField(cp.getValue());
		valueField.setOnAction( e -> { if(rcw.isAutoSend() ) cp.sendNewValue( valueField.getText()); } );
		
		
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

	public void sendAll() {
	}

	public void requestAll() {
	}
	
}
