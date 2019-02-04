package ru.dz.mqtt.viewer;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.config.ConfigurableHost;
import ru.dz.mqtt_udp.config.ConfigurableParameter;

public class RemoteConfigTab extends Tab 
{
	private RemoteConfigWindow rcw;
	private ConfigurableHost ch;
	Label infoLabel = new Label();
	
	private Map<ConfigurableParameter,RemoteConfigControl> controls = new HashMap<ConfigurableParameter, RemoteConfigControl>();
	private String infoSoft;
	private String infoSoftVer;
	private String infoLocation;
	private String infoUptime;

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
		
		//Label infoLabel = new Label( getTabDescription(ch) );
		vbox.getChildren().add( infoLabel );
		updateInfoLabel();
		
		setContent( vbox );
	}

	public String getTabDescription() {
		StringBuilder sb = new StringBuilder( "  Id: "+ ch.getMacAddressString() + "   IP: "+ch.getIpAddressString() );
		
		if( infoSoft != null ) sb.append("   "+infoSoft);
		if( infoSoftVer != null ) sb.append(" v. "+infoSoftVer);
		if( infoLocation != null ) sb.append(" @ "+infoLocation);
		if( infoUptime != null ) sb.append("   up "+infoUptime);		
		//System.out.println(sb);
		
		return sb.toString();
	}

	private void updateInfoLabel()
	{
		infoLabel.setText(getTabDescription());
	}
	
	
	public void updateFromHost(ConfigurableHost newCh) {
		// TODO update tab		
	}

	public void addParameter(ConfigurableParameter cp) {
		
		VBox vbox = (VBox) getContent();

		//ScrollPane scroll = new ScrollPane();
		//scroll.setContent(hbox);
		
		Platform.runLater( new Runnable() {
			@Override
			public void run() { 
				// Display specially
				if(processSpecialParameter(cp))
					return;				
				
				RemoteConfigControl rcc = controls.get(cp);
				if( rcc != null )
				{
					rcc.updateParameter( cp );
					return;
				}

				//vbox.getChildren().add(scroll); 
				RemoteConfigControl newCc = new RemoteConfigControl(cp,rcw);
								
				vbox.getChildren().add( newCc );
				
				controls.put(cp, newCc);
				}
			} );

		
	}
	
	

	private boolean processSpecialParameter(ConfigurableParameter cp) {
		
		if( cp.getKind().equals("node") )
		{
			String name = cp.getName();
			String value = cp.getValue();
			
			switch( name )
			{
			case "name": setText(value); break;
			case "soft": infoSoft = value; break;
			case "ver": infoSoftVer = value; break;
			case "location": infoLocation = value; break;
			case "uptime": infoUptime = value; break;
			
			default: return false;
			}
			
			updateInfoLabel();
			return true;
		}
		
		return false;
	}

	public void sendAll() { 
		//System.out.println("sendAll "+controls.values().size());
		controls.values().forEach( c -> c.sendMe() ); 
		}

	public void requestAll() { controls.values().forEach( c -> c.requestMe() ); }

	// Let topic controls to monitor their topic values 
	public void processPacket(IPacket pkt) {	
		controls.values().forEach( c -> c.processPacket( pkt ) );
	}

	public void afterNetStart() {
		controls.values().forEach( c -> c.afterNetStart() );
	}
	
}
