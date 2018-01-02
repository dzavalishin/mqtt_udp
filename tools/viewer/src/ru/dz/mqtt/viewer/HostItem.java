package ru.dz.mqtt.viewer;

public class HostItem implements Comparable<HostItem>{

	private String hostName;

	public HostItem(String hostName ) {
		this.hostName = hostName;
	}

	@Override
	public String toString() {
		return hostName;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HostItem) {
			HostItem him = (HostItem) obj;
			return him.hostName.equalsIgnoreCase(hostName);
		}

		return false;
	}

	@Override
	public int compareTo(HostItem o) {
		return hostName.compareToIgnoreCase(hostName);
	}
	
	@Override
	public int hashCode() {
		return hostName.hashCode();
	}
	
}
