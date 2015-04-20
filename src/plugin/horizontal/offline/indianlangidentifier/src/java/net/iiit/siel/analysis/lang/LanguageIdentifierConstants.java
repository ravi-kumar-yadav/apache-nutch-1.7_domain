/**
 * 
 */
package net.iiit.siel.analysis.lang;

/**
 * @author niket
 *
 */
public class LanguageIdentifierConstants {

	public static int totalRandomNumberTrials =6 ;
	/**
	 * TODO: "aahe" and ""? and append a space in front or end ..
	 */
	public static String[] marathiDiscriminatingWords={
		"\u0906\u0939\u0947",
		"\u0906\u0923\u093f"		
	};
	
	public static String UKNOWN_LANG="THE_LANG_IS_FOREIGN";

	
	public enum LangShortNames {
		MARATHI("mr"),
		HINDI("hi"),
		GURMUKHI("pa"),
		TAMIL("ta"),
		TELUGU("te"),
		BENGALI("bn"),
		ENGLISH("en"),
		DEVANAGARI("dev"),// this is kept for internal purposes.
		UNKNOWN_LANG("THE_LANG_IS_FOREIGN");
		
		private final String langShortName; 
		public String langShortName()   { 
			java.io.File f = new java.io.File("");
			
			return langShortName;  }
		
	    LangShortNames(String langShortName) {
	        this.langShortName = langShortName;
	}

	}
}
