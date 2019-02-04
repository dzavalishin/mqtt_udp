package ru.dz.mqtt_udp.config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;

/**
 * Node which supports passive mode of remote configuration.
 * 
 * @author dz
 *
 */
public class ConfigurableHost implements Comparable<ConfigurableHost> {

	private String ident;
	private IPacketAddress src;

	public ConfigurableHost(String ident, IPacketAddress src) {
		this.ident = ident;
		this.src = src;
	}

	@Override
	public int hashCode() {
		return ident.hashCode();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ConfigurableHost))
			return false;
		
		ConfigurableHost b = (ConfigurableHost) obj;
			
		return b.ident.equals(ident);
	}

	@Override
	public int compareTo(ConfigurableHost b) {		
		return ident.compareTo(b.ident);
	}

	/**
	 *  Compare all fields, not just ident.
	 * @param ch To compare with
	 * @return true if all the same
	 */
	public boolean isSameAs(ConfigurableHost ch) {
		
		if( ch == null ) return false;
		
		if( !ident.equals(ch.ident) ) return false;
		
		// Now compare just hosts, not ports
		
		//return src.equals(ch.src);
		
		String ipa =    src.getInetAddress().getHostAddress();
		String ipb = ch.src.getInetAddress().getHostAddress();
		
		return ipa.equals(ipb);
	}
	
	@Override
	public String toString() {
		return String.format("%s@%s", ident, src.getInetAddress().getHostAddress() );
	}

	public String getMacAddressString() { return ident; }

	public String getIpAddressString() {
		//return src.getInetAddress().getHostAddress().toString();
		return src.getInetAddress().toString();
	}


	
	public static String getMachineMacAddressString() {
		InetAddress ip;
		try {
				
			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());
			
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
				
			byte[] mac = network.getHardwareAddress();
				
			System.out.print("Current MAC address : ");
				
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				//sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
				sb.append(String.format("%02X", mac[i]) );		
			}
			System.out.println(sb.toString());
			return sb.toString();
				
		} catch (UnknownHostException e) {
			GlobalErrorHandler.handleError(ErrorType.IO, e);
		} catch (SocketException e){				
			GlobalErrorHandler.handleError(ErrorType.IO, e);
		}		
		return null;
	}
	
	
}
