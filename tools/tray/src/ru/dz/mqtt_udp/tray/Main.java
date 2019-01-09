package ru.dz.mqtt_udp.tray;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
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
	private String roomTempVal = "?";
	private String outdoorTempVal = "?";
	
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
			
		if(pp.getTopic() == "RoomTemperature")
			roomTempVal = pp.getValueString();

		if(pp.getTopic() == "OutdoorTemperature")
			outdoorTempVal = pp.getValueString();
		
		tIcon.setToolTip(
				"Room temperature "+roomTempVal+
				"\nOutdoor temperature "+outdoorTempVal
				);		
		
		// TODO if under limit - popup a message?
	}

	private void run() {
		//BufferedImage image = SwingFXUtils.fromFXImage( ImageUtils.getImage16("examples"), null);
		BufferedImage image = ImageUtils.getSwingImage16("examples");
		
		PopupMenu trayMenu = new PopupMenu();
		makeMenu(trayMenu);
		
		//tIcon = new TrayIcon(image,"",menu);
		tIcon = new TrayIcon( image, "mqtt/udp", trayMenu );
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
		
		tIcon.displayMessage("Hello", "World", MessageType.INFO);
		
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

	
	private void makeMenu(PopupMenu trayMenu) {
		{
			MenuItem mi = new MenuItem("Light ON");
			trayMenu.add(mi);
			mi.addActionListener(e -> setItem("Light", "ON"));
		}
		{
			MenuItem mi = new MenuItem("Light OFF");
			trayMenu.add(mi);
			mi.addActionListener(e -> setItem("Light", "OFF"));
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
	            tIcon.displayMessage("Smart House", 
		                "Room temperature "+roomTempVal+
		                "\nOutdoor temperature "+outdoorTempVal,
		                TrayIcon.MessageType.INFO);
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
	
}
