package ru.dz.mqtt_udp.io;

import java.net.InetAddress;

/**
 * 
 * Address packet received from or will be sent to.
 * 
 * Currently just an Internet address.
 * 
 * @author dz
 *
 */

public interface IPacketAddress {

	public String toString();

	public InetAddress getInetAddress();
	
}
