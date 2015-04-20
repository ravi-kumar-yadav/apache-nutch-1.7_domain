/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.iiit.siel.analysis.lang;

// JDK imports
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import net.iiit.siel.analysis.lang.LanguageIdentifierConstants.LangShortNames;
import net.iiit.siel.analysis.lang.NGramProfile.NGramEntry;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseException;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.parse.ParserNotFound;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.ProtocolException;
import org.apache.nutch.protocol.ProtocolFactory;
import org.apache.nutch.protocol.ProtocolNotFound;
import org.apache.nutch.util.NutchConfiguration;

// TODO: Auto-generated Javadoc
/**
 * The Class LanguageIdentifier.
 */
public class LanguageIdentifier {

	/** The Constant DEFAULT_ANALYSIS_LENGTH. */
	private final static int DEFAULT_ANALYSIS_LENGTH = 0; // 0 means full
	// content

	/** The Constant SCORE_THRESOLD. */
	private final static float SCORE_THRESOLD = 0.00F;

	/** The Constant LOG. */
	private final static Log LOG = LogFactory.getLog("Indian Lang Identifier");

	/** The languages. */
	private ArrayList<NGramProfile> languages = new ArrayList<NGramProfile>();

	/** The supported languages. */
	private ArrayList<String> supportedLanguages = new ArrayList<String>();

	/** Minimum size of NGrams. */
	private int minLength = NGramProfile.DEFAULT_MIN_NGRAM_LENGTH;

	/** Maximum size of NGrams. */
	private int maxLength = NGramProfile.DEFAULT_MAX_NGRAM_LENGTH;

	/** The maximum amount of data to analyze. */
	private int analyzeLength = DEFAULT_ANALYSIS_LENGTH;

	/** A global index of ngrams of all supported languages. */
	private HashMap ngramsIdx = new HashMap();

	/** The NGramProfile used for identification. */
	private NGramProfile suspect = null;
	
	/*
	 * DONOT delete the next few lines, they should be enabled, when a lang.
	 * mapping map needs to be generated.  Adding the TextLangMarker
	 * object -- used as a mapping index table for LangId and index of all the
	 * corresponding characters in the text.
	 */
	/*
	 * public TextLangMarker langMarkerObject = new TextLangMarker();
	 */
	/**
	 * Constructs a new Language Identifier.
	 *
	 * @param conf the conf
	 */
	private LanguageIdentifier(Configuration conf) {
		
		conf = NutchConfiguration.create();
		
		// Gets ngram sizes to take into account from the Nutch Config
		minLength = conf.getInt("lang.ngram.min.length",
				NGramProfile.DEFAULT_MIN_NGRAM_LENGTH);
		maxLength = conf.getInt("lang.ngram.max.length",
				NGramProfile.DEFAULT_MAX_NGRAM_LENGTH);
		// Ensure the min and max values are in an acceptale range
		// (ie min >= DEFAULT_MIN_NGRAM_LENGTH and max <=
		// DEFAULT_MAX_NGRAM_LENGTH)
		maxLength = Math.min(maxLength, NGramProfile.ABSOLUTE_MAX_NGRAM_LENGTH);
		maxLength = Math.max(maxLength, NGramProfile.ABSOLUTE_MIN_NGRAM_LENGTH);
		minLength = Math.max(minLength, NGramProfile.ABSOLUTE_MIN_NGRAM_LENGTH);
		minLength = Math.min(minLength, maxLength);

		// Gets the value of the maximum size of data to analyze
		analyzeLength = conf.getInt("lang.analyze.max.length",
				DEFAULT_ANALYSIS_LENGTH);
		System.out.println("Analysis Length is " + analyzeLength);
		String resourceDir=conf.get("resources.indianlangidentifier.dir");
		System.out.println("Resource Directory is " + resourceDir);
		Properties p = new Properties();
		try {
			
			p.load(new FileInputStream(new File(resourceDir + File.separator + "LanguageMappings.properties")));

			Enumeration alllanguages = p.keys();

			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuffer().append(
						"Language identifier configuration [")
						.append(minLength).append("-").append(maxLength)
						.append("/").append(analyzeLength).append("]")
						.toString());
			}

			StringBuffer list = new StringBuffer(
					"Language identifier plugin supports:");
			HashMap tmpIdx = new HashMap();
			while (alllanguages.hasMoreElements()) {
				String lang = (String) (alllanguages.nextElement());

				InputStream is = new FileInputStream(new File(resourceDir + File.separator + lang + "."+ NGramProfile.FILE_EXTENSION));

				if (is != null) {
					NGramProfile profile = new NGramProfile(lang, minLength,
							maxLength);
					try {
						profile.load(is);
						languages.add(profile);
						supportedLanguages.add(lang);
						List ngrams = profile.getSorted();
						for (int i = 0; i < ngrams.size(); i++) {
							NGramEntry entry = (NGramEntry) ngrams.get(i);
							List registered = (List) tmpIdx.get(entry);
							if (registered == null) {
								registered = new ArrayList();
								tmpIdx.put(entry, registered);
							}
							registered.add(entry);
							entry.setProfile(profile);
						}
						list.append(" " + lang + "(" + ngrams.size() + ")");
						is.close();
					} catch (IOException e1) {
						if (LOG.isFatalEnabled()) {
							LOG.fatal(e1.toString());
						}
					}
				}
			}
			// transform all ngrams lists to arrays for performances
			Iterator keys = tmpIdx.keySet().iterator();
			while (keys.hasNext()) {
				NGramEntry entry = (NGramEntry) keys.next();
				List l = (List) tmpIdx.get(entry);
				if (l != null) {
					NGramEntry[] array = (NGramEntry[]) l
							.toArray(new NGramEntry[l.size()]);
					ngramsIdx.put(entry.getSeq(), array);
				}
			}
			if (LOG.isInfoEnabled()) {
				LOG.info(list.toString());
			}
			// Create the suspect profile
			suspect = new NGramProfile("suspect", minLength, maxLength);
			System.out.println("Suspect is " + suspect.getName());
		}
		catch (Exception e) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal(e.toString());
			}
			e.printStackTrace();
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]) {

		String usage = "Usage: LanguageIdentifier "
				+ "[-identifyrows filename maxlines] "
				+ "[-identifyfile charset filename] "
				+ "[-identifyfileset charset files] " + "[-identifytext text] "
				+ "[-identifyurl url]";
		int command = 0;

		final int IDFILE = 1;
		final int IDTEXT = 2;
		final int IDURL = 3;
		final int IDFILESET = 4;
		final int IDROWS = 5;

		Vector fileset = new Vector();
		String filename = "";
		String charset = "";
		String url = "";
		String text = "";
		int max = 0;

		// TODO niket writing test args here..
/*		args = new String[2];
		args[0] = "-identifyurl";
		args[1] = "file:/home1/niket/TamilSamplePage.html";
		//args[2] = "/home1/niket/nutch-clia/input.txt";
*/
		// TODO niket end here

		if (args.length == 0) {
			System.err.println(usage);
			System.exit(-1);
		}

		for (int i = 0; i < args.length; i++) { // parse command line
			if (args[i].equals("-identifyfile")) {
				command = IDFILE;
				charset = args[++i];
				filename = args[++i];
			}

			if (args[i].equals("-identifyurl")) {
				command = IDURL;
				filename = args[++i];
			}

			if (args[i].equals("-identifyrows")) {
				command = IDROWS;
				filename = args[++i];
				max = Integer.parseInt(args[++i]);
			}

			if (args[i].equals("-identifytext")) {
				command = IDTEXT;
				for (i++; i < args.length - 1; i++)
					text += args[i] + " ";
			}

			if (args[i].equals("-identifyfileset")) {
				command = IDFILESET;
				charset = args[++i];
				for (i++; i < args.length; i++) {
					File[] files = null;
					File f = new File(args[i]);
					if (f.isDirectory()) {
						files = f.listFiles();
					} else {
						files = new File[] { f };
					}
					for (int j = 0; j < files.length; j++) {
						fileset.add(files[j].getAbsolutePath());
					}
				}
			}

		}

		Configuration conf = NutchConfiguration.create();
		String lang = null;
		LanguageIdentifier idfr = new LanguageIdentifier(conf);
		File f;
		FileInputStream fis;
		try {
			switch (command) {

			case IDTEXT:
				lang = idfr.identify(text);
				System.out.println("Lang :"+lang);
				break;

			case IDFILE:
				f = new File(filename);
				fis = new FileInputStream(f);
				lang = idfr.identify(fis, charset);
				fis.close();
				break;

			case IDURL:
				lang = LangIdentifierUtility
						.IdentifyLangFromURLDirectly(filename);

				/*
				 * our url identifier is confused or couldn't identify lang from
				 * URL
				 */
				if (lang == null || lang.equalsIgnoreCase("en")) {
					System.out.println("Ambuguity in identifying language from URL");
				}
				else
				{
					System.out.println("Lang was identified(using URL) as: "+lang);
				}
				break;

			case IDROWS:
				f = new File(filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)));
				String line;
				while (max > 0 && (line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 2) {
						max--;
						lang = idfr.identify(line);
						System.out.println("R=" + lang + ":" + line);
					}
				}

				br.close();
				System.exit(0);
				break;

			case IDFILESET:
				/*
				 * used for benchs for (int j=128; j<=524288; j*=2) { long start
				 * = System.currentTimeMillis(); idfr.analyzeLength = j;
				 */
				System.out.println("FILESET");
				Iterator i = fileset.iterator();
				while (i.hasNext()) {
					try {
						filename = (String) i.next();
						f = new File(filename);
						fis = new FileInputStream(f);
						lang = idfr.identify(fis, charset);
						fis.close();
					} catch (Exception e) {
						System.out.println(e);
					}
					System.out.println(filename + " was identified as " + lang);
				}
				/*
				 * used for benchs System.out.println(j + "/" +
				 * (System.currentTimeMillis()-start)); }
				 */
				System.exit(0);
				break;
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("lang could not be identified properly");
			e.printStackTrace();
		}
		System.out.println("text was identified as " + lang);

		/*
		 * DONOT delete the next few lines, they should be enabled, when a lang.
		 * mapping map needs to be generated. TODO  this is for printing
		 * the hashMapRangeLangIDTable only
		 * 
		 * idfr.langMarkerObject.printHashmapTableWithFormatting();
		 * 
		 * System.out
		 * .println("\n\n\n Printing english text contents in this file:\n");
		 * System.out.println(idfr.langMarkerObject.getLangCharacters(
		 * LanguageIdentifierConstants.LangShortNames.ENGLISH
		 * .langShortName()).toString());
		 * 
		 * System.out
		 * .println("\n\n\n Printing telugu text contents in this file:\n");
		 * System.out.println(idfr.langMarkerObject.getLangCharacters(
		 * LanguageIdentifierConstants.LangShortNames.TELUGU
		 * .langShortName()).toString());
		 * 
		 * System.out
		 * .println("\n\n\n Printing unknown text contents in this file:\n");
		 * System.out.println(idfr.langMarkerObject.getLangCharacters(
		 * LanguageIdentifierConstants.LangShortNames.UNKNOWN_LANG
		 * .langShortName()).toString());
		 */
	}

	/**
	 * Gets the url content.
	 *
	 * @param url the url
	 * @param conf the conf
	 * @return the url content
	 */
	// TODO changing the scope to public.
	// TODO Should we use our getURL COntents or use this class' getURLContents
	// feature?
	/*
	 * The function need not get the
	 */
	public static String getUrlContent(String url, Configuration conf) {
		Protocol protocol;
		try {

			protocol = new ProtocolFactory(conf).getProtocol(url);
			Content content = protocol.getProtocolOutput(new Text(url),
					new CrawlDatum()).getContent();
			//System.out.println(content);
			ParseResult parseResult = new ParseUtil(conf).parse(content);
			
			String text = parseResult.get(url).getText();
			System.out.println("Parsed using: " + parseResult + " Text is : " + text);
			return text;

		} catch (ProtocolNotFound e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (ParserNotFound e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Identify url.
	 *
	 * @param url the url
	 * @return the string
	 */
	public String identifyUrl(String url){
		String urllang = LangIdentifierUtility
		.IdentifyLangFromURLDirectly(url);
		return urllang;
	}
	
	/**
	 * Identify.
	 *
	 * @param content the content
	 * @return the string
	 */
	public String identify(String content) {
		return identify(new StringBuffer(content));
	}

	/**
	 * Identify.
	 *
	 * @param content the content
	 * @return the string
	 */
	public String identify(StringBuffer content) {
		// Added
		String lang = "", randomWord;
		StringBuffer text = content;
		if(text.length()<=1)
		return "";

		LanguageIdentifierConstants.LangShortNames[] languagesSampled = new LanguageIdentifierConstants.LangShortNames[LanguageIdentifierConstants.totalRandomNumberTrials];
		/*
		 * We need to analyse "text" now.
		 */

		languagesSampled = getLanguagesSampled(text, LangIdentifierUtility
				.getRandomNumber(text.length() - 1,
						LanguageIdentifierConstants.totalRandomNumberTrials));

		// TODO uncomment the next line For TableMap purpose.(when needed
		// to create a lang-charRange table
		// languagesSampled = getLanguageAndForTaggingWithLangID(text);
		Boolean isNGramReqd = checkIsNGramReqd(languagesSampled);

		/*
		 * if we already identified the right language then don't proceed to
		 * ngram
		 */
		if (!isNGramReqd) {
	//		System.out.println("Language is identified (without ngrams) as: "
	//				+ languagesSampled[0].langShortName());
			/*
			 * DONOT delete the next few lines, they should be enabled, when a
			 * lang. mapping map needs to be generated. Set the
			 * hashmapRangeMarker from start to end .. tag as LangID
			 * 
			 * String rangeMarkerString = this.langMarkerObject
			 * .setLangRangeMarkerTableTillTheEnd(0); if
			 * (!this.langMarkerObject.getLangRangeMarkerTable().containsKey(
			 * languagesSampled[0].langShortName())) { ArrayList<String>
			 * rangeMarkerArrayList = new ArrayList<String>();
			 * rangeMarkerArrayList.add(rangeMarkerString);
			 * this.langMarkerObject.getLangRangeMarkerTable().put(
			 * languagesSampled[0].langShortName(), rangeMarkerArrayList); }
			 * else { ArrayList<String> rangeMarkerArrayList = new
			 * ArrayList<String>(); rangeMarkerArrayList = this.langMarkerObject
			 * .getLangRangeMarkerTable().get(
			 * languagesSampled[0].langShortName());
			 * rangeMarkerArrayList.add(rangeMarkerString);
			 * 
			 * this.langMarkerObject.getLangRangeMarkerTable().put(
			 * languagesSampled[0].langShortName(), rangeMarkerArrayList); }
			 */

			return languagesSampled[0].langShortName();
		}

	//	System.out.print("Using NGP...  ");
		// Code to calculate n-gram profile similarity
		suspect.analyze(text);
		Iterator iter = suspect.getSorted().iterator();
		float topscore = Float.MIN_VALUE;
		HashMap scores = new HashMap();
		NGramEntry searched = null;
		List<String> listOfLang = new ArrayList<String>();
		int decide_point = 0;
		while (iter.hasNext()) {

			searched = (NGramEntry) iter.next();

			NGramEntry[] ngrams = (NGramEntry[]) ngramsIdx.get(searched
					.getSeq());

			/*
			 * Check if ngrams is null, implies that such a sequence of
			 * characters is not found in our profiles, which implies that the
			 * profile is a foreign profile.
			 */
			if (ngrams == null) {
				/*
				 * Check if the searched.getSeq() has a indicUnicode
				 */
				Boolean isForeignLangID = checkCharSequence(searched.getSeq());

				/*
				 * Set the lang as unknown for foreignLangID.
				 */
				if (isForeignLangID) {
					decide_point++;
					lang = LanguageIdentifierConstants.UKNOWN_LANG;
				}

			} 
			if (ngrams != null) {
				for (int j = 0; j < ngrams.length; j++) {
					NGramProfile profile = ngrams[j].getProfile();
					/*
					 * Check when profile is null
					 */
					if (profile == null) {
						profile = new NGramProfile(
								LanguageIdentifierConstants.UKNOWN_LANG,
								NGramProfile.DEFAULT_MIN_NGRAM_LENGTH,
								NGramProfile.DEFAULT_MAX_NGRAM_LENGTH);
					}

					Float pScore = (Float) scores.get(profile);
					if (pScore == null) {
						pScore = new Float(0);
					}
					float plScore = pScore.floatValue();
					plScore += ngrams[j].getFrequency()
							+ searched.getFrequency();
					scores.put(profile, new Float(plScore));

					/*
					 * If the plScore is greater than topScore --> add
					 * the langId to list
					 */
					if (plScore > topscore) {
						topscore = plScore;
						lang = profile.getName();
						/*
						 * Add the lang to list
						 */
						if (!listOfLang.contains(lang)) {
							listOfLang.add(lang);
						}

					}
				}
			}
		}

		if (listOfLang.contains(LanguageIdentifierConstants.UKNOWN_LANG) && decide_point >= (content.length()*0.1)) {
			lang = LanguageIdentifierConstants.UKNOWN_LANG;
		}
		System.out.println("Lang identified thru ngrams test =" + lang);
		return lang;
	}

	/**
	 * Check char sequence.
	 *
	 * @param seq the seq
	 * @return the boolean
	 */
	private Boolean checkCharSequence(CharSequence seq) {

		Boolean isForeign = true;
		char underScore = '_';
		for (int j = 0; j < seq.length(); j++) {
			if (seq.charAt(j) == underScore) {
				continue;
			}
			UnicodeBlock currentUnicode = Character.UnicodeBlock.of(seq
					.charAt(j));

			if (currentUnicode == Character.UnicodeBlock.BENGALI)
				isForeign = false;
			else if (currentUnicode == Character.UnicodeBlock.TELUGU)
				isForeign = false;
			else if (currentUnicode == Character.UnicodeBlock.TAMIL)
				isForeign = false;
			else if (currentUnicode == Character.UnicodeBlock.DEVANAGARI)
				isForeign = false;
			else if (currentUnicode == Character.UnicodeBlock.GURMUKHI)
				isForeign = false;
			else if (currentUnicode == Character.UnicodeBlock.GUJARATI)
				isForeign = false;
			else if (currentUnicode == Character.UnicodeBlock.ORIYA)
				isForeign = false;
			else if (seq.charAt(j) >= 0 && seq.charAt(j) <= 127)
				isForeign = false;
                        /*
 *                          * Return if it is a foreign character, because seq can be composed
 *                                                   * of indic and foreign characters.
 *                                                                            */
                        else
                                return isForeign;
		}
		return isForeign;
	}

	/**
	 * Check is n gram reqd.
	 *
	 * @param languagesSampled the languages sampled
	 * @return the boolean
	 */
	private Boolean checkIsNGramReqd(LangShortNames[] languagesSampled) {
		Boolean isNGramReqd = false;

		/*
		 * Check if the samples of language identified are different, then NGram
		 * is reqd Comparing with the zeroth element
		 */

		for (LangShortNames langShortNames : languagesSampled) {
			if (!langShortNames.equals(languagesSampled[0]) && !isNGramReqd) {
				isNGramReqd = true;
			}
		}
		
		// Bengali and Assamese use the same script ambiguously the unicode block is named BENGALI
		if (!isNGramReqd
				&& languagesSampled[0].equals(LangShortNames.BENGALI)) {
			isNGramReqd = true;
		}

		/*
		 * Check if it is devanagri, then it is reqd.
		 */

		if (!isNGramReqd
				&& languagesSampled[0].equals(LangShortNames.DEVANAGARI)) {
			isNGramReqd = true;
		}

		/*
		 * Check if it is ENGLISH, then it is reqd. The reason: All foreign
		 * language characters are by default called as English
		 */
		if (!isNGramReqd && languagesSampled[0].equals(LangShortNames.ENGLISH)) {
			isNGramReqd = true;
		}

		return isNGramReqd;

	}

	/**
	 * Identify.
	 *
	 * @param is the is
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String identify(InputStream is) throws IOException {
		return identify(is, null);
	}

	/**
	 * Identify.
	 *
	 * @param is the is
	 * @param charset the charset
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String identify(InputStream is, String charset) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int len = 0, i = 0;

		/*
		 * while (((len = is.read(buffer)) != -1) && ((analyzeLength == 0) ||
		 * (out.size() < analyzeLength))) { if (analyzeLength != 0) { len =
		 * Math.min(len, analyzeLength - out.size()); } out.write(buffer, 0,
		 * len); }
		 */
		while (((len = is.read(buffer)) != -1)) {

			// len = Math.min(len, analyzeLength - out.size());

			out.write(buffer, 0, len);
		}
		return identify((charset == null) ? out.toString() : out
				.toString(charset));
	}

	/**
	 * Gets the languages sampled.
	 *
	 * @param content the content
	 * @param index the index
	 * @return the languages sampled
	 */
	
	public LanguageIdentifierConstants.LangShortNames[] getLanguagesSampled(
			StringBuffer content, int[] index) {

		LanguageIdentifierConstants.LangShortNames defaultLang = LanguageIdentifierConstants.LangShortNames.ENGLISH;
		LanguageIdentifierConstants.LangShortNames[] langSamples = new LanguageIdentifierConstants.LangShortNames[LanguageIdentifierConstants.totalRandomNumberTrials];
		int breakpoint = 0;
		for (int i = 0; i < index.length; i++) {

			 // Check that the character is not a special character
			 
			while ((content.charAt(index[i]) > 31
                                        && content.charAt(index[i]) < 65)&& breakpoint<(content.length()>1000?content.length()*0.01:content.length()*0.1)) {
				
				//  Get a new random value
				 
				index[i] = LangIdentifierUtility.getRandomNumber(content
						.length() - 1);
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
	
	/**
	 * Gets the language.
	 *
	 * @param url the url
	 * @param unicodeContent the unicode content
	 * @return the language
	 * @throws UnsupportedLanguageException the unsupported language exception
	 */
	public Language getLanguage(String url, String unicodeContent) throws UnsupportedLanguageException
	{	
		if (LOG.isDebugEnabled()) {
			LOG.debug("== INPUT TO LANGUAGE IDENTIFIER ==");
			LOG.debug("URL: " + url);
			LOG.debug("CONTENT: " + unicodeContent);
		}
//		System.out.println("THis is in LI "+unicodeContent + "great going" );
		String language = this.identifyUrl(url);
		if (LOG.isDebugEnabled()) {
			LOG.debug("LANGUAGE IDENTIFIED THROUGH URL: " + language);
		}

		if (language == null || language.trim().equals("") || language.equalsIgnoreCase("en") || language.equalsIgnoreCase("dev"))
		{
			if (!url.contains("wikipedia.org"))
				language = this.identify(unicodeContent);
		}
		
		if (LOG.isDebugEnabled()) {      
                        LOG.debug("LANGUAGE IDENTIFIED THROUGH CONTENT: " + language);
			LOG.debug("== LANGUAGE IDENTIFICATION ENDS ==");
                }

		if (language.equalsIgnoreCase("en"))
			return Language.ENGLISH;
		else if (language.equalsIgnoreCase("as"))
			return Language.ASSAMESE;
		else if (language.equalsIgnoreCase("bn"))
			return Language.BENGALI;
		else if (language.equalsIgnoreCase("hi"))
			return Language.HINDI;
		else if (language.equalsIgnoreCase("gu"))
			return Language.GUJARATI;
		else if (language.equalsIgnoreCase("mr"))
			return Language.MARATHI;
		else if (language.equalsIgnoreCase("or"))
			return Language.ORIYA;
		else if (language.equalsIgnoreCase("te"))
			return Language.TELUGU;
		else if (language.equalsIgnoreCase("ta"))
			return Language.TAMIL;
		else if (language.equalsIgnoreCase("pa"))
			return Language.PUNJABI;
		else
			throw new UnsupportedLanguageException();
		
	}
	
	/** My singleton instance. */
	private static LanguageIdentifier identifier = null;
	
	/**
	 * Gets the single instance of LanguageIdentifier.
	 *
	 * @return single instance of LanguageIdentifier
	 */
	public static LanguageIdentifier getInstance() {
		if (identifier == null)
			identifier = new LanguageIdentifier(NutchConfiguration.create());
		
		return identifier;
	}
	
	/**
	 * Gets the single instance of LanguageIdentifier.
	 *
	 * @param conf the conf
	 * @return single instance of LanguageIdentifier
	 */
	public static LanguageIdentifier getInstance(Configuration conf) {
		if (identifier == null)
			identifier = new LanguageIdentifier(conf);
		
		return identifier;
	}
}
