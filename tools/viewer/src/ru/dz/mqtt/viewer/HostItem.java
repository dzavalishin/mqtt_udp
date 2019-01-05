package ru.dz.mqtt.viewer;

public class HostItem implements Comparable<HostItem>{

	private String from;
	private String time;

	// TODO IPacketAddress?
	//public HostItem(String hostName ) {		this.hostName = hostName;	}

	public HostItem(TopicItem ti) {
		this.from = ti.getFrom();
		this.time = ti.getTime();
		//ti.get
	}

	@Override
	public String toString() {
		return time+": "+from;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HostItem) {
			HostItem him = (HostItem) obj;
			return him.from.equalsIgnoreCase(from);
		}

		return false;
	}

	@Override
	public int compareTo(HostItem o) {
		return from.compareToIgnoreCase(from);
	}
	
	@Override
	public int hashCode() {
		return from.hashCode();
	}

	public String getHostName() {
		return from;
	}
	
}
