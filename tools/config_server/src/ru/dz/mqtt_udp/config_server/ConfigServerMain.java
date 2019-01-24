package ru.dz.mqtt_udp.config_server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.PacketSourceMultiServer;
import ru.dz.mqtt_udp.config.Provider;

/**
 * 
 * <p>
 * 
 * Remote configuration server main class.
 * 
 * <p>
 * 
 * Supplies requesters with answers to their questions
 * asked with <b>SUBSCRIBE</b> messages.
 * 
 * <p>
 * 
 * @author dz
 *
 */
public class ConfigServerMain {

	private static String propertiesPath = "remote_config.items";

	public static void main(String[] args) {
		
		PacketSourceMultiServer ms = new PacketSourceMultiServer();
		Provider p = new Provider(ms);
		
		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(propertiesPath)) {
		    props.load(in);
		} catch (FileNotFoundException e) {
			System.err.println("File "+propertiesPath+" not found");
			return;
		} catch (IOException e) {
			System.err.println("File "+propertiesPath+" read IO Error: "+e);
			return;
		}

		props.forEach( (key, val) -> {
			p.addTopic((String)key, (String)val);
			System.out.println("Serving "+key+" = "+val);
		} );
		
		ms.requestStart();
		
	}

}
