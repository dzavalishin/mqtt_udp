package ru.dz.mqtt_udp.util;

public class ByteArray {


	public static void dumpBytes(String string, byte[] b) 
	{
		if( b==null )
		{
			System.err.println(string + ", null array " );
			return;
		}

		System.err.println(string + ", len = " + b.length);

		int p = 0;

		while( p < b.length )
		{
			if( (p % 16) == 0 )
				System.err.println("");

			byte cb = b[p++];

			System.err.print( String.format("%02X ", cb) );
		}


		System.err.println("");
		System.err.println("--");
	}	
	
}
