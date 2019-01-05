package ru.dz.mqtt_udp.io;

import java.net.DatagramSocket;
import java.net.SocketException;

public class SingleSendSocket {

        private static volatile SingleSendSocket instance;

        /**
         * Singleton for UDP send socket
         * @return socket to send with
         */
        public static DatagramSocket get()  
        {
		SingleSendSocket localInstance = instance;
		if (localInstance == null) {
			synchronized (SingleSendSocket.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new SingleSendSocket();
				}
			}
		}
		return localInstance.sock;
        }

		private DatagramSocket sock;
		
		private SingleSendSocket() 
		{
			//sock = GenericPacket.sendSocket();
			try {
				sock = sendSocket();
			} catch (SocketException e) {
				throw new RuntimeException("Can't create send socket", e);
			}
		}

		
		/**
		 * Create new socket to send MQTT/UDP packets.
		 * @return socket
		 * @throws SocketException
		 */
		private static DatagramSocket sendSocket() throws SocketException
		{
			DatagramSocket s = new DatagramSocket();
			s.setBroadcast(true);
			return s;
		}
		
}

