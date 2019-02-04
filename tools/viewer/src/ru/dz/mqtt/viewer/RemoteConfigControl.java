package ru.dz.mqtt.viewer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.event.EventHandler;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.config.ConfigurableParameter;
import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;
import ru.dz.mqtt_udp.util.image.ImageUtils;

public class RemoteConfigControl extends HBox 
{
	private static final String REMOTE_VALUE_IS_THE_SAME = "Remote value is the same";
	private ConfigurableParameter cp;
	//private RemoteConfigWindow rcw;

	private TextField valueField = new TextField();
	private Button diffButton;
	private Label messageLabel = new Label( "" );

	private String remoteValue;
	private boolean remoteValueSame = true;

	private final ImageView remoteEquals = ImageUtils.getIcon("Folder-Accept");
	private final ImageView remoteDiffers = ImageUtils.getIcon("Folder-Warning");
	private String contentForTopic = null;

	public RemoteConfigControl(ConfigurableParameter cp, RemoteConfigWindow rcw) 
	{
		super(20); // i am hbox, set interval between nodes

		this.cp = cp;
		//this.rcw = rcw;

		remoteValue = cp.getValue();
		ifTopicRequestValue(); 

		Label kindLabel = new Label(cp.getKind()+":");
		kindLabel.setPrefWidth(30);

		Label nameLabel = new Label(cp.getName() );
		nameLabel.setPrefWidth(50);
		nameLabel.setStyle("-fx-font-weight: bold;");

		valueField.setText(cp.getValue());
		valueField.setOnAction( e -> { if(rcw.isAutoSend() ) cp.sendNewValue( valueField.getText()); } );

		// runLater to run AFTER event is processed inside field
		valueField.setOnKeyTyped( e-> Platform.runLater( new Runnable() {			
			@Override
			public void run() {
				checkRemoteSame();				
			}
		} ) );
		//valueField.setOnKeyPressed( e->checkRemoteSame() );



		getChildren().add(kindLabel);
		getChildren().add(nameLabel);
		getChildren().add(valueField);

		makeButton( ImageUtils.getIcon("options"), "Send to network", e -> cp.sendNewValue( valueField.getText()) );
		makeButton( ImageUtils.getIcon("order"), "Request from network", e -> cp.requestAgain() );

		diffButton = makeButton( remoteEquals, REMOTE_VALUE_IS_THE_SAME, e -> updateLocalFromRemote() );
		diffButton.setDisable(true);

		//messageLabel.setPrefWidth(100);
		messageLabel.setStyle("-fx-font-weight: bold;");
		getChildren().add(messageLabel);
	}

	private void setRemoteValueDiffers()
	{
		diffButton.setGraphic( remoteDiffers );
		diffButton.setDisable(false);
		diffButton.setTooltip( new Tooltip("Update FROM remote value"));
	}

	private void setRemoteValueEquals()
	{
		diffButton.setGraphic( remoteEquals );
		diffButton.setDisable(true);
		diffButton.setTooltip( new Tooltip(REMOTE_VALUE_IS_THE_SAME) );
	}

	private Button makeButton( ImageView icon, String toolTip, EventHandler<ActionEvent> handler )
	{
		final Button cellButton = new Button();

		cellButton.setGraphic(icon);
		cellButton.setTooltip(new Tooltip(toolTip));

		cellButton.setOnAction(handler);

		getChildren().add(cellButton);

		return cellButton;
	}

	public void sendMe() { 
		//System.out.println("sendMe "+cp);
		cp.sendNewValue( valueField.getText() ); 
	}

	public void requestMe() { cp.requestAgain(); }


	// Possibly new value - called by incoming network traffic
	public void updateParameter(ConfigurableParameter newCp) {
		remoteValue = newCp.getValue();
		checkRemoteSame();
		ifTopicRequestValue();
	}

	public void checkRemoteSame() {
		remoteValueSame = remoteValue.equals( valueField.getText() );

		if( remoteValueSame ) setRemoteValueEquals();
		else setRemoteValueDiffers();

		updateMessage();
	}

	private void updateLocalFromRemote() {
		valueField.setText(remoteValue);
		//setRemoteValueEquals();
		checkRemoteSame();
	}


	private void updateMessage()
	{
		//System.out.println("upd "+remoteValue+ " val "+contentForTopic);

		if(!remoteValueSame)
		{
			messageLabel.setText("Remote: '"+remoteValue+"'");
			return;
		}

		if( contentForTopic != null )
		{
			messageLabel.setText("Topic value: '"+contentForTopic+"'");
			return;
		}
		
		messageLabel.setText("");
	}

	public void processPacket(IPacket pkt) {	
		if (!(pkt instanceof PublishPacket))
			return;

		PublishPacket pp = (PublishPacket) pkt;

		/*
		System.out.println(String.format("pp.topic '%s' rem val '%s' kind '%s'", 
				pp.getTopic(),
				remoteValue,
				cp.getKind()
				));*/
		
		if(cp.getKind().equals("topic") && pp.getTopic().equals(remoteValue))
		{
			contentForTopic = pp.getValueString();
			System.out.println(String.format("pp.topic '%s' val '%s' ", pp.getTopic(), contentForTopic) );
			
			Platform.runLater( new Runnable() {			
				@Override
				public void run() { updateMessage(); }
			} );
		}
	}


	/**
	 * If my kind is 'topic' request corresponding topic value from network
	 */
	private void ifTopicRequestValue()
	{
		if( !cp.getKind().equals("topic") )
			return;
		
		try {
			if(remoteValue != null)
			{
				//System.out.println("subscribe "+remoteValue);
				new SubscribePacket(remoteValue).send();
			}
		} catch (IOException e) {
			GlobalErrorHandler.handleError(ErrorType.IO, e);
		}
	}

	/**
	 * Called when network listen is active.
	 */
	public void afterNetStart() {
		ifTopicRequestValue();
	}

}
