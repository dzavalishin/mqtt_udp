package ru.dz.mqtt_udp.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.Optional;

import ru.dz.mqtt_udp.IPacket;
import ru.dz.mqtt_udp.MqttProtocolException;
import ru.dz.mqtt_udp.io.IPacketAddress;
import ru.dz.mqtt_udp.io.IpAddress;
import ru.dz.mqtt_udp.io.SingleSendSocket;
import ru.dz.mqtt_udp.proto.TTR_PacketNumber;
import ru.dz.mqtt_udp.proto.TTR_ReplyTo;
import ru.dz.mqtt_udp.proto.TTR_Signature;
import ru.dz.mqtt_udp.proto.TaggedTailRecord;

/**
 * Network IO work horse for MQTT/UDP packets.
 * @author dz
 *
 */

public abstract class GenericPacket implements IPacket {

	/** 
	 * Broadcast IP address.
	 */
	private static final byte[] broadcast =  { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF } ;
	
	/**
	 * Packet header flags.
	 */
	protected byte    flags = 0;
	
	/**
	 * Packet source address, if packet is received from net. 
	 * Locally created ones have null here.
	 */
	protected IPacketAddress from;
	
	/**
	 * Construct packet from network.
	 * @param from Sender's address.
	 */
	protected GenericPacket(IPacketAddress from) {
		this.from = from;
	}

	/**
	 * Construct packet to be sent.
	 */
	protected GenericPacket() {
		this.from = null;
	}
	
	/**
	 * Create new socket to send MQTT/UDP packets.
	 * @return socket
	 * @throws SocketException
	 * /
	public static DatagramSocket sendSocket() throws SocketException
	{
		DatagramSocket s = new DatagramSocket();
		s.setBroadcast(true);
		return s;
	} */

	/**
	 * Create new socket to listen to MQTT/UDP packets.
	 * @return Created socket.
	 * @throws SocketException If unable.
	 */
	public static DatagramSocket recvSocket() throws SocketException
	{
		//DatagramSocket s = new DatagramSocket(mqtt_udp_defs.MQTT_PORT);
		DatagramSocket s = new DatagramSocket(null);
		//s.setBroadcast(true);
		
		s.setReuseAddress(true);
		// TODO reuseport
		
		InetSocketAddress address = new InetSocketAddress(mqtt_udp_defs.MQTT_PORT);
		
		s.bind(address);
		return s;
	}

	/**
	 * Broadcast me using default send socket.
	 * @throws IOException If unable.
	 */
	public void send() throws IOException
	{
		send(SingleSendSocket.get());
	}
	
	/**
	 * Send me using default send socket.
	 * @throws IOException If unable.
	 */
	public void send(InetAddress addr) throws IOException {
		send(SingleSendSocket.get(),addr);
	}
	
	/**
	 * Broadcast me using given socket. 
	 * @param sock Socket must be made with sendSocket() method.
	 * @throws IOException If unable.
	 */
	public void send(DatagramSocket sock) throws IOException
	{
		byte[] pkt = toBytes();
		
		InetAddress address = InetAddress.getByAddress(broadcast);
		DatagramPacket p = new DatagramPacket(pkt, pkt.length, address, mqtt_udp_defs.MQTT_PORT);
		sock.send(p);
	}

	/**
	 * Send me to given address. 
	 * @param sock Socket must be made with sendSocket() method.
	 * @param address Host to send to
	 * @throws IOException If unable.
	 */
	public void send(DatagramSocket sock, InetAddress address) throws IOException
	{
		byte[] pkt = toBytes();
		
		DatagramPacket p = new DatagramPacket(pkt, pkt.length, address, mqtt_udp_defs.MQTT_PORT);
		sock.send(p);
		//System.out.println("UDP sent "+pkt.length);
	}

	

	/**
	 * Wait for packet to come in.
	 * @return Packet received.
	 * @throws SocketException As is.
	 * @throws IOException As is. 
	 * @throws MqttProtocolException What we got is not a valid MQTT/UDP packet.
	 * /
	public static IPacket recv() throws SocketException, IOException, MqttProtocolException
	{
		DatagramSocket s = recvSocket();
		IPacket o = recv(s);
		s.close();
		return o;
	}*/

	
	/**
	 * Wait for packet to come in.
	 * @param s Socket to use.
	 * @return Packet received.
	 * @throws SocketException As is.
	 * @throws IOException As is. 
	 * @throws MqttProtocolException What we got is not a valid MQTT/UDP packet.
	 */
	public static IPacket recv(DatagramSocket s) throws IOException, MqttProtocolException
	{
		// some embedded systems can't fragment UDP and
		// fragmented UDP is highly unreliable anyway, so it is 
		// better to stick to MAC layer max packet size 
		
		byte[] buf = new byte[2*1024];  
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		
		s.receive(p);

		int l = p.getLength();
		
		byte[] got = new byte[l];  
		
		System.arraycopy(p.getData(), p.getOffset(), got, 0, l);
		
		return IPacket.fromBytes(got, new IpAddress(p.getSocketAddress()) );		
	}


	/*
	 * (non-Javadoc)
	 * @see ru.dz.mqtt_udp.IPacket#getFrom()
	 */
	@Override
	public IPacketAddress getFrom() { return from; }
	
	/**
	 * Get packet flags. QoS, etc.
	 */
	public byte getFlags() {		return flags;	}
	
	@Override
	public String toString() {		
		return String.format("MQTT/UDP packet of unknown type from '%s', please redefine toString in %s", from, getClass().getName());
	}


	/**
	 * <p>
	 * <b>Internal use only.</b>
	 * </p>
	 * <p>
	 * Apply data from TTRs to constructed packet.
	 * </p>
	 * @param ttrs Tagged Tail Records to apply
	 * @return Self
	 */
	public IPacket applyTTRs(Collection<TaggedTailRecord> ttrs)
	{
		if( ttrs == null )
			return this;
		
		for( TaggedTailRecord ttr : ttrs )
			applyTTR(ttr);
		
		return this;
	}

	private void applyTTR(TaggedTailRecord ttr) 
	{
		if (ttr instanceof TTR_Signature) {
			// just ignore, checked outside
		}
		
		else if (ttr instanceof TTR_PacketNumber) {
			TTR_PacketNumber t = (TTR_PacketNumber) ttr;			
			setPacketNumber( t.getValue() );
		}
		
		else if (ttr instanceof TTR_ReplyTo) {
			TTR_ReplyTo r = (TTR_ReplyTo) ttr;			
			setReplyToPacketNumber( r.getValue() );
		}
		
		else 
		{
			GlobalErrorHandler.handleError(ErrorType.Protocol, "Unknown TTR: "+ttr);
		}
	}
	

	private Optional<Integer> replyToPacketNumber = Optional.empty();

	public Optional<Integer> getReplyToPacketNumber() {
		return replyToPacketNumber;
	}

	public void setReplyToPacketNumber( int replyToPacketNumber ) {
		this.replyToPacketNumber = Optional.ofNullable( replyToPacketNumber );
	}
	

	
	
	private Optional<Integer> packetNumber = Optional.empty();

	public Optional<Integer> getPacketNumber() {
		return packetNumber;
	}

	public void setPacketNumber(int packetNumber) {
		this.packetNumber = Optional.ofNullable(packetNumber);
	}

	
}
