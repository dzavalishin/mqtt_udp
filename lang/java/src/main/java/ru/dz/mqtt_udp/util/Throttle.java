package ru.dz.mqtt_udp.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * <p>
 * Send throttle
 * <p>
 * Will keep send pace. Up to {max_seq_packets} will be passed with no pause,
 * but more than that will be paused so that average time between packets
 * will be about {throttle} msec.
 * 
 * 
**/
public class Throttle 
{

	private long last_send_time = 0;
	private AtomicLong last_send_count = new AtomicLong(0);
	
	
	static private long time_msec()
	{
		return System.currentTimeMillis();
	}

	/**
	 * up to 3 packets can be sent with no throttle
	 * most devices have some buffer and we do not
	 * want to calc time each send
	**/
	private int max_seq_packets = 3; 

	/**
	 * Time between outgoing packets
	 */
	private int throttle = 100;

	
	/**
	 * Set packet send rate.
	 * @param msec average time in milliseconds between packets. Set to 0 to turn throttling off.
	 */
	public void setThrottle(int msec)
	{
	    throttle = msec;
	}
	
	/**
	 * Must be called in packet send code.
	 * Will put caller asleep to make sure packets are sent in a right pace. 
	 */
	public void throttle() 
	{

	    if( throttle == 0 )
	        return;

	    // Let max_seq_packets come through with no pause.
	    long last_send_count_delta = last_send_count.incrementAndGet();
	    if( last_send_count_delta < max_seq_packets )
	        return;

	    //last_send_count.addAndGet(-last_send_count_delta); // eat out
	    last_send_count.addAndGet(max_seq_packets);

	    long now = time_msec();
	    //print( str(now) )
	    long since_last_pkt = now - last_send_time;

	    if( last_send_time == 0 )
	    {
	        last_send_time = now;
	        return;
	    }
	    
	    last_send_time = now;

	    long towait = max_seq_packets * throttle - since_last_pkt;

	    // print( str(towait) )

	    if( towait <= 0 )
	        return;

	    try {
			Thread.sleep(towait);
		} catch (InterruptedException e) {
			GlobalErrorHandler.handleError(ErrorType.Timeout, e);
		}
	        		
	}

}





