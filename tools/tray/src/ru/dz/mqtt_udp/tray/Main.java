package ru.dz.mqtt_udp.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import ru.dz.mqtt_udp.util.image.ImageUtils;

public class Main {

	private static TrayIcon tIcon;
	private SystemTray tray = SystemTray.getSystemTray();
	
	public static void main(String[] args) 
	{
		new Main().run();
		
	}

	private void run() {
		//BufferedImage image = SwingFXUtils.fromFXImage( ImageUtils.getImage16("examples"), null);
		BufferedImage image = ImageUtils.getSwingImage16("examples");
		
		PopupMenu trayMenu = new PopupMenu();
		makeMenu(trayMenu);
		
		//tIcon = new TrayIcon(image,"",menu);
		tIcon = new TrayIcon( image, "mqtt/udp", trayMenu );
		tIcon.setImageAutoSize(true);
		
		
		
		ActionListener actionListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            tIcon.displayMessage("Action Event", 
	                "An Action Event Has Been Performed!",
	                TrayIcon.MessageType.INFO);
	        }
	    };
		tIcon.addActionListener(actionListener);
			
		//tIcon.addMouseListener(makeMouseListener());
		
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
		MenuItem mi1 = new MenuItem("Test");
		trayMenu.add(mi1);
	}

	private MouseListener makeMouseListener()
	{
		MouseListener mouseListener = new MouseListener() {
            
	        public void mouseClicked(MouseEvent e) {
	            System.out.println("Tray Icon - Mouse clicked!");                 
	        }
	 
	        public void mouseEntered(MouseEvent e) {
	            System.out.println("Tray Icon - Mouse entered!");                 
	        }
	 
	        public void mouseExited(MouseEvent e) {
	            System.out.println("Tray Icon - Mouse exited!");                 
	        }
	 
	        public void mousePressed(MouseEvent e) {
	            System.out.println("Tray Icon - Mouse pressed!");                 
	        }
	 
	        public void mouseReleased(MouseEvent e) {
	            System.out.println("Tray Icon - Mouse released!");                 
	        }
	    };		
	    
	    return mouseListener;
	}
	
}
