package ru.dz.mqtt_udp.tray;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.PacketSourceServer;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.util.image.ImageUtils;

public class Main {

	private static TrayIcon tIcon;
	private static SystemTray tray = SystemTray.getSystemTray();
	private String topic1Val = "?";
	private String topic2Val = "?";

	private String topic1 = "PLK0_activePa";
	private String topic2 = "PLK0_Va";
	private String topic1Header = "Power consumption";
	private String topic2Header = "Mains Voltage";

	private String userMessage = "No data received";
	private String controlTopic = "GroupGuestMain";

	public static void main(String[] args) 
	{
		Main m = new Main();
		m.run();
		PacketSourceServer ss = new PacketSourceServer();
		ss.setSink( pkt -> m.processPkt( pkt ));	

	}

	private void processPkt(IPacket pkt) {
		if(!(pkt instanceof PublishPacket))
			return;

		PublishPacket pp = (PublishPacket) pkt;

		System.out.println("topic = "+pp.getTopic());

		if(pp.getTopic().equals( topic1 ))
			topic1Val = pp.getValueString();

		if(pp.getTopic().equals( topic2 ))
			topic2Val = pp.getValueString();

		userMessage = 
				topic1Header +" "+topic1Val+
				"\n"+
				topic2Header +" "+topic2Val;				

		tIcon.setToolTip( userMessage );		

		// TODO if under limit - popup a message?
	}

	private void run() {
		//BufferedImage image = SwingFXUtils.fromFXImage( ImageUtils.getImage16("examples"), null);
		BufferedImage image = ImageUtils.getSwingImage16("examples");

		PopupMenu trayMenu = new PopupMenu();
		makeMenu(trayMenu);

		//tIcon = new TrayIcon(image,"",menu);
		tIcon = new TrayIcon( image, "", trayMenu );
		tIcon.setImageAutoSize(true);


		/*
		ActionListener actionListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            tIcon.displayMessage("Action Event", 
	                "An Action Event Has Been Performed!",
	                TrayIcon.MessageType.INFO);
	        }
	    };
		tIcon.addActionListener(actionListener);
		 */	

		tIcon.addMouseListener(makeMouseListener());



		SystemTray tray = SystemTray.getSystemTray();


		try {
			tray.add(tIcon);
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//tIcon.displayMessage("Hello", "World", MessageType.INFO);

	}

	private void makeMenu(PopupMenu trayMenu) {
		{
			MenuItem mi = new MenuItem("Light ON");
			trayMenu.add(mi);
			mi.addActionListener(e -> setItem( controlTopic, "ON"));
		}
		{
			MenuItem mi = new MenuItem("Light OFF");
			trayMenu.add(mi);
			mi.addActionListener(e -> setItem( controlTopic, "OFF"));
		}
		trayMenu.addSeparator();
		{
			MenuItem mi = new MenuItem("Exit");
			trayMenu.add(mi);
			mi.addActionListener(e -> stopMe());
		}
	}

	private void setItem(String topic, String val) {
		try {
			new PublishPacket(topic, val).send();
		} catch (IOException e) {
			// TODO Logger? 
			e.printStackTrace();
		};
	}

	private void stopMe() {
		tray.remove(tIcon);
		System.exit(0);
	}




	private MouseListener makeMouseListener()
	{
		MouseListener mouseListener = new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				//System.out.println("Tray Icon - Mouse clicked!");
				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) 
				{
					tIcon.displayMessage(
							"Smart House", userMessage,
							TrayIcon.MessageType.INFO);
				}
			}


			public void mouseEntered(MouseEvent e) {
				//System.out.println("Tray Icon - Mouse entered!");                 
			}

			public void mouseExited(MouseEvent e) {
				//System.out.println("Tray Icon - Mouse exited!");                 
			}

			public void mousePressed(MouseEvent e) {
				//System.out.println("Tray Icon - Mouse pressed!");                 
			}

			public void mouseReleased(MouseEvent e) {
				//System.out.println("Tray Icon - Mouse released!");                 
			}
		};		

		return mouseListener;
	}

	/*
	 * 
	private void sleepForever()
	{
		while(true)
		{
			try {
				synchronized (this) {					
					wait(1000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	 
	 *
	 */
	
}
