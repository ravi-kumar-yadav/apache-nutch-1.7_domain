/**
 * 
 */
package net.iiit.siel.analysis.lang;

import java.lang.Character.UnicodeBlock;
import java.util.Random;

import net.iiit.siel.analysis.lang.LanguageIdentifierConstants.LangShortNames;

/**
 * 
 * @author niket
 * 
 */
public class LangIdentifierUtility {

	/**
	 * 
	 * @param UpperRange
	 * @param howManyRandomNumNeeded
	 * @return
	 */
	public static int[] getRandomNumber(int UpperRange,
			int howManyRandomNumNeeded) {
		
		Random generator = new Random();
		

		int[] randomNumber = new int[howManyRandomNumNeeded];
		for (int i = 0; i < randomNumber.length; i++) {
			randomNumber[i] = generator.nextInt(UpperRange);
		}

		return randomNumber;
	}

	/**
	 * 
	 * @return
	 */
	public static int getRandomNumber(int UpperRange) {
		Random generator = new Random();
		int randomNumber = 0;
		randomNumber = generator.nextInt(UpperRange);
		return randomNumber;
	}

	/**
	 * 
	 * @return
	 */
	public static int getRandomNumber() {
		Random generator = new Random();
		int randomNumber = 0;
		randomNumber = generator.nextInt();
		return randomNumber;
	}

	public static String IdentifyLangFromURLDirectly(String url) {
		/*
		 * TokeniseURL
		 */
		
		/*
		 * Check for the domain of the URL
		 * If the domain is in the ignore list, then ignore the URL and return STOP_LANG
		 * If the domain is what we want, (if in, com, org .. then proceed)
		 * If the domain is in a regional lang. e.g. te.wikipedia.org -- then call that particular lang. identifier using ngrams.
		 */
		URLToken tempObject = new URLToken(url);
		tempObject.computeDomainName();		

		if(tempObject.getIndicDomain()!=null)
		{
			return tempObject.getIndicDomain();
		}
		/*
		if(tempObject.getForeignDomain()!=null)
		{
			return tempObject.getForeignDomain();
		}
		
		if(tempObject.getAmbiguousDomain()!=null)
		{
			
			
		}
		*/
		
		/*
		 * Check for encoding in a regional language
		 */

		int startIndex = url.indexOf('/');
		if(startIndex<0)
		{
			startIndex = 0;
		}
		/*
		 * TODO we should check for only the last part of the URL?.
		 */
		tempObject.computeLastPartOfURL();

		if(tempObject.getLastPartOfURL()!= null || tempObject.getLastPartOfURL()!=""|| tempObject.getLastPartOfURL().length()>0)
		{

		//LangShortNames[] theLanguage = getLanguage(new StringBuffer(url.substring(startIndex, url.length()-1)), LangIdentifierUtility.getRandomNumber(url.length()-1, LanguageIdentifierConstants.totalRandomNumberTrials));
			/*
			 * Boundary condition
			 */
			int upperRange = 0;
			upperRange = tempObject.getLastPartOfURL().length();
			if(upperRange<=0)
			{
				/*
				 * The random numbers would not be generated hence no point in invoking the getLang func
				 */
				return null;
			}
		LangShortNames[] theLanguage = getLanguage(new StringBuffer(tempObject.getLastPartOfURL()), LangIdentifierUtility.getRandomNumber(upperRange, LanguageIdentifierConstants.totalRandomNumberTrials));

		if(!theLanguage[0].equals(LangShortNames.ENGLISH)){
			return theLanguage[0].langShortName();
		}
		
		}

		
		/*
		 * Check for the name of the url, if it contains the language names
		 
		for (LangShortNames namesOfLang : LangShortNames.values()){
			if(url.indexOf(namesOfLang.toString().toLowerCase())>0)
			{
				System.out.println("DONE. It is "+namesOfLang.langShortName());
				return namesOfLang.langShortName();
			}
		}*/
		
		/*
		 * Removing the ngram profile from this class, .. keeping it in LangIdentifier class itself
		 * N-gram profile 
		 
		Configuration conf = NutchConfiguration.create();
		String lang = null;
		LanguageIdentifier idfr = new LanguageIdentifier(conf);
		lang = idfr.identify(url);
				*/
		/*
		 * Not able to determine using URL. so return null
		 */
		return null;
		
	}
	
	public static String tokenizeURL(String url)
	{
		String tokenizedURL= "";
		/*
		 * Remove all special characters
		 */
		String stopWords[] = new String[]{
				":",
				"/",
				"-",
				"_",
				"?"
		};
		
		for (int i = 0; i < stopWords.length; i++) {
			if(url.contains(stopWords[i])){
				url.replace(" ",stopWords[i]);
			}
		}
		
		//tokenizedURL = url.split(" ");
		
		return tokenizedURL;
		
	}
	
	public static LanguageIdentifierConstants.LangShortNames[] getLanguage(
			StringBuffer content, int[] index) {
		LanguageIdentifierConstants.LangShortNames defaultLang = LanguageIdentifierConstants.LangShortNames.ENGLISH;
		LanguageIdentifierConstants.LangShortNames[] langSamples = new LanguageIdentifierConstants.LangShortNames[LanguageIdentifierConstants.totalRandomNumberTrials];
		int breakpoint = 0;
		for (int i = 0; i < index.length; i++) {

			/*
			 * Check that the character is not a special character
			 */
			while((index[i]<0 && content.charAt(index[i]) > 31 &&    content.charAt(index[i]) < 65) && breakpoint<(content.length()>1000?content.length()*0.01:content.length()*0.1))
                        {
				/*
				 * Get a new random value
				 */
				index[i] = LangIdentifierUtility.getRandomNumber(content.length()-1);
				breakpoint++;	
			}

			UnicodeBlock currentUnicode = Character.UnicodeBlock.of(content
					.charAt(index[i]));

			if (currentUnicode == Character.UnicodeBlock.BENGALI)
				langSamples[i] = LanguageIdentifierConstants.LangShortNames.BENGALI;
			else if (currentUnicode == Character.UnicodeBlock.TELUGU)
				langSamples[i] = LanguageIdentifierConstants.LangShortNames.TELUGU;
			else if (currentUnicode == Character.UnicodeBlock.TAMIL)
				langSamples[i] = LanguageIdentifierConstants.LangShortNames.TAMIL;
			else if (currentUnicode == Character.UnicodeBlock.DEVANAGARI)
				langSamples[i] = LanguageIdentifierConstants.LangShortNames.DEVANAGARI;
			else if (currentUnicode == Character.UnicodeBlock.GURMUKHI)
				langSamples[i] = LanguageIdentifierConstants.LangShortNames.GURMUKHI;
			else
				langSamples[i] = defaultLang;

		}

		return langSamples;
}

}
