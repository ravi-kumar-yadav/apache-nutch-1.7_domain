package net.iiit.siel.analysis.lang;

public class UnsupportedLanguageException extends Exception 
{ 
	public UnsupportedLanguageException()
	{
		super("Identified language is not currently supported by CLIA system");
	
	}
	

}
