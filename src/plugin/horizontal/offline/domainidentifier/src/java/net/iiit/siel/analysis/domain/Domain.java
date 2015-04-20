package net.iiit.siel.analysis.domain;

public enum Domain {
	TOURISM("tourism"), HEALTH("health"), UNSUPPORTED("misc");
	
	String domain;
	
	private Domain(String domain) {
		this.domain = domain;
	}
	
	public String toString() {
		return this.domain;
	}
}
