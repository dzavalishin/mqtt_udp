package test.ru.dz.mqtt_udp;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.dz.mqtt_udp.TopicFilter;

public class TopicFilterTest {

	@Test
	public void testPlain() 
	{
		TopicFilter tf = new TopicFilter("aaa/ccc/bbb");
		assertTrue( tf.test("aaa/ccc/bbb") );
		assertFalse( tf.test("aaa/c/bbb") );
		assertFalse( tf.test("aaa/ccccc/bbb") );
		assertFalse( tf.test("aaa/ccccc/ccc") );
	}

	@Test
	public void testPlus() 
	{
		TopicFilter tf = new TopicFilter("aaa/+/bbb");
		assertTrue( tf.test("aaa/ccc/bbb") );
		assertTrue( tf.test("aaa/c/bbb") );
		assertTrue( tf.test("aaa/ccccc/bbb") );
		assertFalse( tf.test("aaa/ccccc/ccc") );
	}

	@Test
	public void testSharp() 
	{
		TopicFilter tf = new TopicFilter("aaa/#");
		assertTrue( tf.test("aaa/ccc/bbb") );
		assertTrue( tf.test("aaa/c/bbb") );
		assertTrue( tf.test("aaa/ccccc/bbb") );
		assertFalse( tf.test("aba/ccccc/ccc") );
	}
	
	
}
