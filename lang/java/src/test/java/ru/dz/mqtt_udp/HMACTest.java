package ru.dz.mqtt_udp;

import static org.junit.Assert.*;

import org.junit.Test;

public class HMACTest {

	private String key = "key";
	private String msg = "text";
	
	@Test
	public void testHmacDigestMD5() {
		String ok = "d0ca6177c61c975fd2f8c07d8c6528c6";
		assertEquals(ok, ru.dz.mqtt_udp.hmac.HMAC.hmacDigestMD5(msg, key));
	}

	@Test
	public void testHmacDigestSHA256() {
		String ok = "6afa9046a9579cad143a384c1b564b9a250d27d6f6a63f9f20bf3a7594c9e2c6";
		assertEquals(ok, ru.dz.mqtt_udp.hmac.HMAC.hmacDigestSHA256(msg, key));
	}

}
