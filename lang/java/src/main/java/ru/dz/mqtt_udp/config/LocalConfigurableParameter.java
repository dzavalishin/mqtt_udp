package ru.dz.mqtt_udp.config;



public class LocalConfigurableParameter extends ConfigurableParameter {

	public LocalConfigurableParameter(String kind, String name, String value) {
		super(new LocalConfigurableHost(), kind, name, value);
		// TODO Auto-generated constructor stub
	}

	
	static class LocalConfigurableHost extends ConfigurableHost
	{
		private static String id = makeId();

		public LocalConfigurableHost() {
			super(id, null);

		}

		// Attempt to make some GUID-like thing
		private static String makeId() 
		{			
			long time = System.currentTimeMillis();			
			return ConfigurableHost.getMachineMacAddressString()+":"+String.format("%X", time);
		}
		
	}
	
}
