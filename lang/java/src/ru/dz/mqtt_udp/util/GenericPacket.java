package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.io.IpAddress;

public abstract class GenericPacket implements IPacket {

	//private static final int  MQTT_PORT = 1883;
	private static final byte[] broadcast =  { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF } ;
	
	protected byte    flags;
	protected IPacketAddress from = null;
	
	/**
	 * Create new socket to send MQTT/UDP packets.
	 * @return socket
	 * @throws SocketException
	 */
	public static DatagramSocket sendSocket() throws SocketException
	{
		DatagramSocket s = new DatagramSocket();
		s.setBroadcast(true);
		return s;
	}

	/**
	 * Create new socket to listen to MQTT/UDP packets.
	 * @return socket
	 * @throws SocketException
	 */
	public static DatagramSocket recvSocket() throws SocketException
	{
		//DatagramSocket s = new DatagramSocket(mqtt_udp_defs.MQTT_PORT);
		DatagramSocket s = new DatagramSocket(null);
		//s.setBroadcast(true);
		
		s.setReuseAddress(true);
		// TODO reuseport
		
		InetSocketAddress address = new InetSocketAddress(mqtt_udp_defs.MQTT_PORT);
		
		s.bind(address);
		return s;
	}

	/**
	 * Create socket, broadcast me, delete socket.
	 * @throws IOException
	 */
	public void send() throws IOException
	{
		DatagramSocket s = sendSocket();
		send(s);
		s.close();
	}
	
	/**
	 * Broadcast me using given socket. 
	 * @param sock Socket must be made with sendSocket() method.
	 * @throws IOException
	 */
	public void send(DatagramSocket sock) throws IOException
	{
		byte[] pkt = toBytes();
		
		InetAddress address = InetAddress.getByAddress(broadcast);
		DatagramPacket p = new DatagramPacket(pkt, pkt.length, address, mqtt_udp_defs.MQTT_PORT);
		sock.send(p);
	}

	/**
	 * Send me to given address. 
	 * @param sock Socket must be made with sendSocket() method.
	 * @param address Host to send to
	 * @throws IOException
	 */
	public void send(DatagramSocket sock, InetAddress address) throws IOException
	{
		byte[] pkt = toBytes();
		
		DatagramPacket p = new DatagramPacket(pkt, pkt.length, address, mqtt_udp_defs.MQTT_PORT);
		sock.send(p);
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
		
		return IPacket.fromBytes(got, new IpAddress(p.getSocketAddress()) );		
	}


	public IPacketAddress getFrom() { return from; }
	
	@Override
	public String toString() {		
		return String.format("MQTT/UDP packet of unknown type from '%s', please redefine toString in %s", from, getClass().getName());
	}
	
}
