package ru.dz.mqtt_udp.relcom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.IPacketMultiSource;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.PubAckPacket;
import ru.dz.mqtt_udp.io.SingleSendSocket;
import ru.dz.mqtt_udp.util.ErrorType;
import ru.dz.mqtt_udp.util.GenericPacket;
import ru.dz.mqtt_udp.util.GlobalErrorHandler;
import ru.dz.mqtt_udp.util.LoopRunner;

public class OutgoingPackets implements Consumer<IPacket> {

	protected static final int RESEND_COUNT = 3;


	private static final int MIN_ACK = 3;


	//private List<GenericPacket> outgoing = new ArrayList<GenericPacket>(); 
	private Map<Integer,GenericPacket> outgoing = new HashMap<Integer,GenericPacket>();

	public OutgoingPackets(IPacketMultiSource s) {
		lr.requestStart();
		s.addPacketSink(this);
	}

	void add( GenericPacket p)
	{
		int qos = p.getQoS();

		// No QoS? No resend.
		if( qos == 0 )
			return;

		synchronized (outgoing) {
			if( !p.getPacketNumber().isPresent() )
				GlobalErrorHandler.handleError(ErrorType.Protocol, "no packet id in OutgoingPackets");
			outgoing.put( p.getPacketNumber().get(), p);
		}
	}

	
	@Override
	public void accept(IPacket p) {
		if (p instanceof PubAckPacket) {
			PubAckPacket pa = (PubAckPacket) p;
			
			if( !pa.getReplyToPacketNumber().isPresent() )
				return;
			
			int replyTo = pa.getReplyToPacketNumber().get();
			
			GenericPacket found = outgoing.get(replyTo);
			if( found == null )
				return;
			
			System.out.println("found "+replyTo);
			
			int ackQoS = pa.getQoS();
			int sentQoS = found.getQoS();
			
			if( ackQoS == sentQoS )
			{
				outgoing.remove(replyTo);
				System.out.println("qos equal, remove");
				return;
			}
			
			if( ackQoS == sentQoS-1 )
			{
				System.out.println("qos -1, decrement");
				found.incrementAckCount();
				if( found.getAckCount() > MIN_ACK )
				{
					outgoing.remove(replyTo);
					System.out.println("ack count reached, remove");
					return;
				}
			}
			
		}
		
	}
	
		
	
	private LoopRunner lr = new LoopRunner("ResendLoop") {

		@Override
		protected void step() throws IOException, MqttProtocolException {
			sleep(300); // TODO use sleep time from defs
			synchronized (outgoing) 
			{
				for(GenericPacket p : outgoing.values()) {
					p.resend(SingleSendSocket.get());
				}

				boolean retry = false;
				while(true)
				{
					for(GenericPacket p : outgoing.values()) {
						//p.getSentCounter() > mqtt_udp_defs.RESEND_COUNT
						if( p.getSentCounter() > RESEND_COUNT )
						{
							outgoing.remove(p);
							retry = true;
							break;
						}
					}
					
					if(!retry)
						break;
				}



			}
		}

		/**
		 * Unused
		 */
		@Override
		protected void onStop() throws IOException, MqttProtocolException { /* empty */ }

		/**
		 * Unused
		 */
		@Override
		protected void onStart() throws IOException, MqttProtocolException { /* empty */ }
	};

}
