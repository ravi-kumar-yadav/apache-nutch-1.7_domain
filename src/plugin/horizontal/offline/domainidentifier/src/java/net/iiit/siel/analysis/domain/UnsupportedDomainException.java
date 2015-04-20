package net.iiit.siel.analysis.domain;

public class UnsupportedDomainException extends Exception {
	public UnsupportedDomainException() {
		super("Content doesn't belong to any of the supported domains");
	}
}
