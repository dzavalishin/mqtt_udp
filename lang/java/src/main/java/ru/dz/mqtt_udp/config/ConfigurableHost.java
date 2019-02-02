package ru.dz.mqtt_udp.config;

import ru.dz.mqtt_udp.io.IPacketAddress;

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

	
}
