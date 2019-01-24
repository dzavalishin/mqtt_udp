package ru.dz.mqtt_udp.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;


import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.PublishPacket;
import ru.dz.mqtt_udp.SubscribePacket;
import ru.dz.mqtt_udp.items.AbstractItem;
import ru.dz.mqtt_udp.items.TopicItem;

/**
 * <p>Remote configuration requester.</p>
 * 
 * <p>Keeps set of topics, requests them from network, keeps replies. Usage:</p>
 * <pre>
 * 
 * IPacketMultiSource ms = new PacketSourceMultiServer();
 * ms.start();
 * 
 * Requester r = Requester(ms);
 * 
 * r.addTopic("$SYS/myinstancename/param"); // will request it from net
 *
 * r.startBackgroundRequests(); // start asking for topic values in loop
 * 
 * if( !waitForAll(10*1000*1000) )
 * {
 * 		print("Can't get config from net"); System.Exit(1);
 * }
 * 
 * </pre>
 * @author dz
 *
 */

public class Requester implements Consumer<IPacket> {
	private static final int CHECK_LOOP_TIME = 1000*60;
	private static final int REQUEST_STEP_TIME = 1000;

	private long checkLoopTime = CHECK_LOOP_TIME;
	

	private Map<String,TopicItem> items = new HashMap<>();

	/**
	 * Construct.
	 * @param ms MQTT/UDP network listener which is able to serve multiple consumers.
	 */
	public Requester(IPacketMultiSource ms) 
	{
		ms.addPacketSink(this);	
	}

	/**
	 * Add topic, which value is to be requested from MQTT/UDP network,
	 * @param topicName name of topic to request
	 * @throws IOException if network send is failed
	 */
	public void addTopic(String topicName) throws IOException
	{
		synchronized (items) {

			// TODO need class PublishTopicItem?
			//items.put(topicName, new TopicItem(mqtt_udp_defs.PTYPE_PUBLISH, topicName, topicValue));
			items.put(topicName, null);

			SubscribePacket sp = new SubscribePacket(topicName);
			sp.send();
		}
	}

	/**
	 * Get value for topic
	 * @param topic to get value for
	 * @return value or null if not yet received
	 */
	public String getValue(String topic)
	{
		String v = null;
		synchronized (items) {
			TopicItem ti = items.get(topic);
			if( ti != null )
				v = ti.getValue();
		}		
		return v;
	}

	/**
	 * Implementation of Consumer&lt;IPacket&gt; interface.
	 * Sink to put received packets to.
	 */
	@Override
	public void accept(IPacket t) {
		if( !(t instanceof PublishPacket) ) 
			return;

		PublishPacket pp = (PublishPacket) t;

		synchronized (items) {
			if( !items.containsKey(pp.getTopic()) )
				return;

			TopicItem ai = (TopicItem) AbstractItem.fromPacket(pp);
			//System.out.println("REQUESTER: Got reply for "+ai.getTopic());
			items.put(ai.getTopic(), ai);
		}
	}


	/**
	 * Start background process to poll net for topics we need.
	 */
	public void startBackgroundRequests() {
		Runnable target = makeLoopRunnable();
		Thread t = new Thread(target, "MQTT UDP config.Requester");
		t.start();
	}

	/**
	 * Set time between repeated requests for items.
	 * @param checkLoopTime Time in milliseconds.
	 */
	public void setCheckLoopTime(long checkLoopTime) {		this.checkLoopTime = checkLoopTime;	}
	public long getCheckLoopTime() {		return checkLoopTime;	}

	

	private Runnable makeLoopRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				while(true)
				{
					try {
						// Re-request once a minute
						Thread.sleep( checkLoopTime );
						loop();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}
		};
	}


	protected void loop() throws IOException {

		// find topics for which we have no data
		Set<String> empty = getAllEmpty();

		// request them one per second		
		for( String topic : empty )
		{
			new SubscribePacket(topic).send();
			try {
				Thread.sleep(REQUEST_STEP_TIME);
			} catch (InterruptedException e) {
				// Ignore
			}
		}

	}

	/**
	 * Get list of topics for which we do not have data yet.
	 * @return Set of topic strings
	 */
	public Set<String> getAllEmpty() {
		Set<String> empty = new HashSet<String>();

		synchronized (items) {
			items.forEach( (topic, item) -> {
				if( item == null )
					empty.add(topic);
			});
		}
		return empty;
	}

	/**
	 * Check if all items got values.
	 * @return true if we got all data.
	 */
	public boolean isDone()
	{
		synchronized (items) {
			for( TopicItem item : items.values() )
				if( item == null )
					return false;
		}
		return true;
	}
	
	/**
	 * Wait for all topics to get content. Returns when we
	 * have data for all topics we know about.
	 * 
	 * @param timeoutMsec Max time to wait.
	 * 
	 * @return true if success, false if timed out.
	 */
	public boolean waitForAll(long timeoutMsec)
	{
		if( timeoutMsec < 0 )
			throw new IllegalArgumentException("timeoutMsec < 0");

		long start = System.currentTimeMillis();

		while(true)
		{
			//System.out.print("Wait 4 all loop");
			/*
			Set<String> e = getAllEmpty();
			if( e.size() == 0 )
				return true;
			*/
			
			if( isDone() ) return true;
			
			long now = System.currentTimeMillis();

			if( (now - start) > timeoutMsec )
				return false;

			try {
				//Thread.sleep(CHECK_LOOP_TIME/2); // TODO sleep on cond signalled in recv for shorter time
				Thread.sleep(timeoutMsec/5);
			} catch (InterruptedException e1) {
				// Ignore
			}
		}
	}

	// TODO set sink to be informed on arrive of some item or any item
	
}
