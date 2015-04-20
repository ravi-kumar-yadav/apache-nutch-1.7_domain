package net.iiit.siel.analysis.lang;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * This class is used as a generic URL-Token. We can get different information
 * about the URL from this data structure
 * 
 * Attributes: domain name - String shouldLeverage -Boolean <e.g. If it is a
 * German URL, then we wont leverage it.> FirstPart of the URL - String
 * Remaining part of the URL - String LastPart of the URL - String <e.g. useful
 * in getting the language of the URL wikipedia.org/topuc/telegu> isValidURL -
 * Boolean URLString - String
 * 
 * Functions: Setters and getters. Computing the different components of the URL
 * 
 * Constructor: URLToken(String URL)
 * 
 * @author niket
 * @since 2009/06/17
 */
public class URLToken {

	private String domainName;
	private String firstPartOfURL;
	private String foreignDomain;
	private String ambiguousDomain;
	private String indicDomain;
	private String otherThanFirstPartOfURL;
	private String lastPartOfURL;
	private Boolean isValidURL;
	private String urlString;
	private StringTokenizer urlStringTokenizerObject;
	private String stopWords[] = new String[] { ":", "/", "-", "_", "?" };
	private String originalURL;

	/**
	 * @return the originalURL
	 */
	public String getOriginalURL() {
		return originalURL;
	}

	/**
	 * @param originalURL
	 *            the originalURL to set
	 */
	public void setOriginalURL(String originalURL) {
		this.originalURL = originalURL;
	}

	/*
	 * Used to clean all useless words like www etc
	 */
	private String wordsNotUseful[] = new String[] { // "www",
	"http", "https",
	// "html",
	// "htm"
	};
	/*
	 * Used to clean all foreign domains like www etc -- 179 domains
	 */
	private String predefinedForeignDomains[] = new String[] { "aa", "ab",
			"ae", "af", "ak", "am", "an", "ar", "av", "ay", "az", "ba",
			"be", "bg", "bh", "bi", "bm", "bo", "br", "bs", "ca", "ce", "ch",
			"cn", "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz",
			"ee", "el", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo",
			"fr", "fy", "ga", "gd", "gl", "gn", "gv", "ha", "he", "ho",
			"hr", "ht", "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik",
			"io", "is", "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk",
			"kl", "km", "kn", "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la",
			"lb", "lg", "li", "ln", "lo", "lt", "lu", "lv", "mg", "mh", "mi",
			"mk", "ml", "mn", "mo", "ms", "mt", "my", "na", "nb", "nd", "ne",
			"ng", "nl", "nn", "no", "nr", "nv", "ny", "oj", "om",
			"os", "pi", "pl", "ps", "pt", "qu", "rm", "rn", "ro", "ru", "rw",
			"sa", "sc", "sd", "se", "sg", "sh", "si", "sk", "sl", "sm", "sn",
			"so", "sq", "sr", "ss", "st", "su", "sv", "sw", "tg", "th", "ti",
			"tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ty", "ug", "uk",
			"ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi", "yo", "za",
			"zh", "zu" };
	/*
	 * Used to find all ambiguous domains like in, com, org etc
	 */
	private String predefinedAmbiguousDomains[] = new String[] { "in", "com", "org" };
	/*
	 * Used to find all known indian domains like te,hi,ta etc
	 */
	private String predefinedIndicsDomains[] = new String[] {"as", "gu", "or", "en", "te", "mr", "hi", "pa", "ta", "bn"};

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * @param domainName
	 *            the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * @param domainName
	 *            the domainName to compute
	 */
	public String computeDomainName() {
		String domainNameComputed = "";
		Boolean isItForeignDomain = false;
		Boolean isItIndicDomain = false;

		if (this.getFirstPartOfURL() != null || this.getFirstPartOfURL() != "") {
			this.computefirstPartOfURL();
		}
		domainNameComputed = getFirstPartOfURL();

		String domainNameThruLangEquals = computeDomainUsingLangEqualsInURL();
		String dot = ".";
		/*
		 * Check foreignDomain
		 */

		for (int i = 0; i < predefinedForeignDomains.length; i++) {
			if ((domainNameComputed.startsWith(predefinedForeignDomains[i]
					+ dot) || domainNameComputed.endsWith(dot
					+ predefinedForeignDomains[i]))
					|| (domainNameThruLangEquals
							.equalsIgnoreCase(predefinedForeignDomains[i]))) {

				/*
				 * We will compute the domain name as "UNKNOWN"
				 * setForeignDomain(predefinedForeignDomains[i]);
				 * domainNameComputed = predefinedForeignDomains[i];
				 */
				setForeignDomain(LanguageIdentifierConstants.UKNOWN_LANG);
				domainNameComputed = LanguageIdentifierConstants.UKNOWN_LANG;
				isItForeignDomain = true;
				/*
				 * Job done, leave this loop
				 */
				break;
			}
		}

		/*
		 * Check indicDomain
		 */
		for (int i = 0; !isItForeignDomain
				&& i < predefinedIndicsDomains.length; i++) {
			if (domainNameComputed.startsWith(predefinedIndicsDomains[i] + dot)
					|| domainNameComputed.endsWith(dot
							+ predefinedIndicsDomains[i])
					|| (domainNameThruLangEquals
							.equalsIgnoreCase(predefinedIndicsDomains[i]))) {

				setIndicDomain(predefinedIndicsDomains[i]);
				domainNameComputed = predefinedIndicsDomains[i];

				/*
				 * Job done, leave this loop
				 */
				isItIndicDomain = true;
				break;
			}
		}

		/*
		 * Check ambiguousDomain
		 */
		for (int i = 0; !isItForeignDomain && !isItIndicDomain
				&& i < predefinedAmbiguousDomains.length; i++) {
			if (domainNameComputed.startsWith(predefinedAmbiguousDomains[i]
					+ dot)
					|| domainNameComputed.endsWith(dot
							+ predefinedAmbiguousDomains[i])

			) {

				setAmbiguousDomain(predefinedAmbiguousDomains[i]);
				domainNameComputed = predefinedAmbiguousDomains[i];
				/*
				 * Job done, leave this loop
				 */
				break;
			}
		}

		/*
		 * set the domain name
		 */
		setDomainName(domainNameComputed);
		return domainNameComputed;
	}

	private String computeDomainUsingLangEqualsInURL() {
		String domainName = "";
		String hardCodedLangExpr = "lang=";
		int index = 0;
		if ((index = getOriginalURL().indexOf(hardCodedLangExpr)) != -1) {
			/*
			 * Return 2 character after this index
			 */
			int start = index + hardCodedLangExpr.length();
			int end = start + 2;
			if (end <= getOriginalURL().length()) {
				domainName = getOriginalURL().substring(start, end);
				return domainName;
			}
		}
		return domainName;
	}

	/**
	 * @return the foreignDomain
	 */
	public String getForeignDomain() {
		return foreignDomain;
	}

	/**
	 * @param foreignDomain
	 *            the foreignDomain to set
	 */
	public void setForeignDomain(String foreignDomain) {
		this.foreignDomain = foreignDomain;
	}

	/**
	 * @return the ambiguousDomain
	 */
	public String getAmbiguousDomain() {
		return ambiguousDomain;
	}

	/**
	 * @param ambiguousDomain
	 *            the ambiguousDomain to set
	 */
	public void setAmbiguousDomain(String ambiguousDomain) {
		this.ambiguousDomain = ambiguousDomain;
	}

	/**
	 * @return the indicDomain
	 */
	public String getIndicDomain() {
		return indicDomain;
	}

	/**
	 * @param indicDomain
	 *            the indicDomain to set
	 */
	public void setIndicDomain(String indicDomain) {
		this.indicDomain = indicDomain;
	}

	/**
	 * @return the firstPartOfURL
	 */
	public String getFirstPartOfURL() {
		return firstPartOfURL;
	}

	/**
	 * @param firstPartOfURL
	 *            the firstPartOfURL to set
	 */
	public void setFirstPartOfURL(String firstPartOfURL) {
		this.firstPartOfURL = firstPartOfURL;
	}

	/**
	 * @param firstPartOfURL
	 *            the firstPartOfURL to compute
	 */
	public String computefirstPartOfURL() {
		String firstPartOfURLComputed = null;

		Boolean hasFirstPartOfURLBeenFound = false;

		/*
		 * Use regular expression.
		 */
		String regularExpression = "(http(://)?(s://)?)?";
		firstPartOfURLComputed = urlString.replaceFirst(regularExpression, "");
		this.urlString = firstPartOfURLComputed;

		this.urlStringTokenizerObject = new StringTokenizer(
				firstPartOfURLComputed, "/");
		while (!hasFirstPartOfURLBeenFound
				&& this.urlStringTokenizerObject.hasMoreElements()) {
			firstPartOfURLComputed = this.urlStringTokenizerObject.nextToken();
			hasFirstPartOfURLBeenFound = true;
		}

		/*
		 * set the first part of the URL
		 */

		setFirstPartOfURL(firstPartOfURLComputed);

		/*
		 * We can also set the remaining part of string here itself.
		 */
		otherThanFirstPartOfURL = urlString.substring(
				urlString.indexOf(firstPartOfURLComputed)
						+ firstPartOfURLComputed.length()).trim();
		if (otherThanFirstPartOfURL.startsWith("/")) {
			otherThanFirstPartOfURL = otherThanFirstPartOfURL.substring(1);
		}

		setOtherThanFirstPartOfURL(otherThanFirstPartOfURL);

		return firstPartOfURLComputed;
	}

	/**
	 * @return the otherThanFirstPartOfURL
	 */
	public String getOtherThanFirstPartOfURL() {
		return otherThanFirstPartOfURL;
	}

	/**
	 * @param otherThanFirstPartOfURL
	 *            the otherThanFirstPartOfURL to set
	 */
	public void setOtherThanFirstPartOfURL(String otherThanFirstPartOfURL) {
		this.otherThanFirstPartOfURL = otherThanFirstPartOfURL;
	}

	/**
	 * @param otherThanFirstPartOfURL
	 *            the otherThanFirstPartOfURL to compute
	 */

	public ArrayList<String> computeFragmentsOfotherThanFirstPartOfURL() {
		ArrayList<String> fragmentsOfOtherThanFirstPartOfURLComputed = new ArrayList<String>();

		this.urlStringTokenizerObject = new StringTokenizer(
				otherThanFirstPartOfURL, "/");
		while (this.urlStringTokenizerObject.hasMoreElements()) {
			fragmentsOfOtherThanFirstPartOfURLComputed
					.add(this.urlStringTokenizerObject.nextToken());

		}

		return fragmentsOfOtherThanFirstPartOfURLComputed;
	}

	/**
	 * @return the lastPartOfURL
	 */
	public String getLastPartOfURL() {
		return lastPartOfURL;
	}

	/**
	 * @param lastPartOfURL
	 *            the lastPartOfURL to set
	 */
	public void setLastPartOfURL(String lastPartOfURL) {
		this.lastPartOfURL = lastPartOfURL;
	}

	/**
	 * @param lastPartOfURL
	 *            the lastPartOfURL to compute
	 */
	public String computeLastPartOfURL() {
		String lastPartOfURLComputed = "";

		while (this.urlStringTokenizerObject.hasMoreElements()) {
			lastPartOfURLComputed = this.urlStringTokenizerObject.nextToken();
		}

		if (lastPartOfURLComputed.trim().startsWith("/"))
			lastPartOfURLComputed = lastPartOfURLComputed.substring(1);
		/*
		 * Set the value
		 */
		setLastPartOfURL(lastPartOfURLComputed);

		return lastPartOfURLComputed;
	}

	/**
	 * @return the isValidURL
	 */
	public Boolean getIsValidURL() {
		return isValidURL;
	}

	/**
	 * @param isValidURL
	 *            the isValidURL to set
	 */
	public void setIsValidURL(Boolean isValidURL) {
		this.isValidURL = isValidURL;
	}

	/**
	 * @param isValidURL
	 *            the isValidURL to compute
	 */
	public Boolean computeIsValidURL() {
		Boolean isValidURLComputed = true;
		if (originalURL == null || originalURL.contains(" ")
				|| originalURL.trim().equals("")) {
			/*
			 * Can do a lot more like checking here
			 */
			isValidURLComputed = false;
		}
		setIsValidURL(isValidURLComputed);
		return isValidURLComputed;
	}

	/**
	 * @return the urlString
	 */
	public String getUrlString() {
		return urlString;
	}

	/**
	 * @param urlString
	 *            the urlString to set
	 */
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	/*
	 * Constructor
	 */
	public URLToken(String URL) {
		this.originalURL = URL;
		this.urlString = URL;
	}

	public static void main(String[] args) {
		String URLValue1 = "http://www.jexamples.com/srchRe/java.util.StringTokenizer.new&lang=es";
		String URLValue2 = "www.yahoo.com/sports";
		String URLValue3 = "te.wikipedia.org";

		URLToken tempObject = new URLToken(URLValue1);
		tempObject.computefirstPartOfURL();
		tempObject.computeLastPartOfURL();
		tempObject.computeDomainName();

		System.out.println("First Part of URL computed = "
				+ tempObject.getFirstPartOfURL() + "\n"
				+ "Last part of URL computed = "
				+ tempObject.getLastPartOfURL());
		System.out.println("domain =" + tempObject.getDomainName());
		System.out.println("indic domain =" + tempObject.getIndicDomain());
		System.out.println("foreign domain =" + tempObject.getForeignDomain());
		System.out.println("ambiguous domain ="
				+ tempObject.getAmbiguousDomain());

	}

}
