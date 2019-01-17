package ru.dz.mqtt_udp;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.dz.mqtt_udp.proto.TTR_PacketNumber;

public class TestTTR_Number {

	private static final int TEST_VALUE = 0x33;
	static final byte[] test = { (byte)'n', 4, 0, 0, 0, 0x33 };
	static final byte[] netin = { 0, 0, 0, 0x33 };

	@Test
	public void testToBytes() {
		TTR_PacketNumber r = new TTR_PacketNumber( TEST_VALUE );
		
		byte[] bytes = r.toBytes();
		assertArrayEquals(test, bytes);
	}

	@Test
	public void testTTR_NumberByteByteArrayInt() {
		TTR_PacketNumber r = new TTR_PacketNumber( (byte)'n', netin, 4 );
		assertEquals(TEST_VALUE, r.getValue());
	}

}
