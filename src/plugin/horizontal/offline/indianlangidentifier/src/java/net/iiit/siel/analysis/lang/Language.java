package net.iiit.siel.analysis.lang;

public enum Language {
	ASSAMESE("as"),
	BENGALI("bn"),
	ENGLISH("en"),
	GUJARATI("gu"),
	HINDI("hi"),
	MARATHI("mr"),
	ORIYA("or"),
	PUNJABI("pa"),
	TAMIL("ta"),
	TELUGU("te"),
	UNSUPPORTED("un");
	
	private String lang;
	
	private Language(String lang) {
		this.lang = lang;
	}
	
	public String toString() {
		return this.lang;
	}
}
