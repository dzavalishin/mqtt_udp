package ru.dz.mqtt_udp.tray;

import java.awt.SystemTray;
import java.awt.TrayIcon;

public class Main {

	private static TrayIcon tIcon;
	private SystemTray tray = SystemTray.getSystemTray();
	
	public static void main(String[] args) {
	
		//tIcon = new TrayIcon(image,"",menu);
		tIcon = new TrayIcon(null,"mqtt/udp",null);

	}

}
