package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ru.dz.mqtt_udp.IPacket;

public abstract class GenericPacket implements IPacket {

	private static final int  MQTT_PORT = 1883;
	private static final byte[] broadcast =  { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF } ;

	public DatagramSocket sendSocket() throws SocketException
	{
		DatagramSocket s = new DatagramSocket();
		s.setBroadcast(true);
		return s;
	}

	public DatagramSocket recvSocket() throws SocketException
	{
		DatagramSocket s = new DatagramSocket(MQTT_PORT);
		//s.setBroadcast(true);
		return s;
	}

	public void send() throws IOException
	{
		send(sendSocket());
	}
	
	public void send(DatagramSocket s) throws IOException
	{
		byte[] pkt = toBytes();
		
		InetAddress address = InetAddress.getByAddress(broadcast);
		DatagramPacket p = new DatagramPacket(pkt, pkt.length, address, MQTT_PORT);
		s.send(p);
	}

}
