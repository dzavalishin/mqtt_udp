package test.ru.dz.mqtt_udp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ru.dz.mqtt_udp.TopicFilter;

class TopicFilterTest {

	@Test
	void testTest() {
		fail("Not yet implemented");
		
		{
			TopicFilter tf = new TopicFilter("aaa/+/bbb");
			assertTrue( tf.test("aaa/ccc/bbb") );
			assertTrue( tf.test("aaa/c/bbb") );
			assertTrue( tf.test("aaa/ccccc/bbb") );
			assertFalse( tf.test("aaa/ccccc/bbb") );
		}
		
	}

}
