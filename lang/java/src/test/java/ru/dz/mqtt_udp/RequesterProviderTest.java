package ru.dz.mqtt_udp;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ru.dz.mqtt_udp.config.Provider;
import ru.dz.mqtt_udp.config.Requester;

public class RequesterProviderTest {

	private static final String T1  = "test/java/t1";
	private static final String T1V = "test value 1";

	private static final String T2  = "test/java/t3";
	private static final String T2V = "test value 2";

	private static final String T3  = "test/java/t3";
	private static final String T3V = "test value 3";

	private static final String TO = "timeout/";
	

	private static PacketSourceMultiServer ms;
	private static Provider p;
	private static Requester r;
	
	@BeforeClass
    public static void setUpClass() throws Exception 
	{
		ms = new PacketSourceMultiServer();
		p = new Provider(ms);		
		r = new Requester(ms);

		ms.requestStart();		
    }

	
	
	@Test(timeout=4000) //@Ignore
	public void testExchange() throws IOException {
		
		p.addTopic(T1, T1V);
		p.addTopic(T2, T2V);
		p.addTopic(T3, T3V);
		
		r.addTopic(T1);
		r.addTopic(T2);
		r.addTopic(T3);
		
		assertTrue( r.waitForAll(1000) );
	}

	
	@Test //@Ignore
	public void testTimeout() throws IOException {
		
		
		r.setCheckLoopTime(1000);
		r.startBackgroundRequests();
		
		// Let requester ask it for the first time before 
		// provider is ready to answer. It will work if
		// requester is repeating it's requests
		r.addTopic(TO+T1);
		r.addTopic(TO+T2);
		r.addTopic(TO+T3);

		p.addTopic(TO+T1, T1V);
		p.addTopic(TO+T2, T2V);
		p.addTopic(TO+T3, T3V);		
		
		assertTrue( r.waitForAll(4000) );
	}

	
}
