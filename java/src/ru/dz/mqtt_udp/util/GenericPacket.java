package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;

public abstract class GenericPacket implements IPacket {

	//private static final int  MQTT_PORT = 1883;
	private static final byte[] broadcast =  { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF } ;
	
	protected String from;
	
	public static DatagramSocket sendSocket() throws SocketException
	{
		DatagramSocket s = new DatagramSocket();
		s.setBroadcast(true);
		return s;
	}

	public static DatagramSocket recvSocket() throws SocketException
	{
		DatagramSocket s = new DatagramSocket(mqtt_udp_defs.MQTT_PORT);
		//s.setBroadcast(true);
		return s;
	}

	public void send() throws IOException
	{
		DatagramSocket s = sendSocket();
		send(s);
		s.close();
	}
	
	public void send(DatagramSocket s) throws IOException
	{
		byte[] pkt = toBytes();
		
		InetAddress address = InetAddress.getByAddress(broadcast);
		DatagramPacket p = new DatagramPacket(pkt, pkt.length, address, mqtt_udp_defs.MQTT_PORT);
		s.send(p);
	}

	
	public static IPacket recv() throws SocketException, IOException, MqttProtocolException
	{
		DatagramSocket s = recvSocket();
		IPacket o = recv(s);
		s.close();
		return o;
	}

	
	public static IPacket recv(DatagramSocket s) throws IOException, MqttProtocolException
	{
		// some embedded systems can't fragment UDP and
		// fragmented UDP is highly unreliable anyway, so it is 
		// better to stick to MAC layer max packet size 
		
		byte[] buf = new byte[2*1024];  
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		
		s.receive(p);

		int l = p.getLength();
		
		byte[] got = new byte[l];  
		
		System.arraycopy(p.getData(), p.getOffset(), got, 0, l);
		
		return IPacket.fromBytes(got, p.getSocketAddress().toString());		
	}


	public String getFrom() { return from; }
	
	@Override
	public String toString() {		
		return String.format("MQTT/UDP packet of unknown type from '%s', please redefine toString in %s", from, getClass().getName());
	}
	
}
