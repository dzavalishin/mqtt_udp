package ru.dz.mqtt_udp.io;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class IpAddress extends GeneralAddress implements Comparable<IPacketAddress>{

	private SocketAddress socketAddress;

	public IpAddress(SocketAddress socketAddress)
	{
		this.socketAddress = socketAddress;
		
	}
	
	@Override
	public String toString() {
		if (socketAddress instanceof InetSocketAddress) {
			InetSocketAddress sa = (InetSocketAddress) socketAddress;
	
			return sa.getHostString();
		}
		
		return socketAddress.toString();
	}

	@Override
	public int compareTo(IPacketAddress o) {
		if (o instanceof IpAddress) {
			IpAddress ia = (IpAddress) o;

			return ia.socketAddress.toString().compareTo(toString());
		}
		
		return o.getClass().hashCode() - getClass().hashCode();
	}
	
}
