package net.iiit.siel.analysis.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import net.iiit.siel.analysis.lang.Language;
import net.iiit.siel.analysis.lang.LanguageIdentifier;
import net.iiit.siel.analysis.lang.UnsupportedLanguageException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class DomainIdentifier 
{
	// Singleton Instance
	private static DomainIdentifier domainIdentifier = null;
	
	private static final Log LOG = LogFactory.getLog(DomainIdentifier.class);
	
	private HashMap<Language, Classifier> classifierMap = new HashMap<Language, Classifier>();
	private HashMap<Language, Instances> instancesMap = new HashMap<Language, Instances>();
	private Configuration conf;
	private String resourceDir;
	
	private DomainIdentifier(Configuration conf) {
		this.conf = conf;
		DIResourceManager rm = DIResourceManager.getInstance(conf);
		
		try {
			for (Language lang : Language.values()) {
				if (lang == Language.UNSUPPORTED)
					continue;
				
				DataSource ds = new DataSource(rm.getARFFPath(lang));
				Instances instances = ds.getStructure();
				if (instances.classIndex() == -1)
					instances.setClassIndex(instances.numAttributes() - 1);
				
				this.instancesMap.put(lang, instances);
				
				Classifier classifier = (Classifier)SerializationHelper.read(rm.getModelPath(lang));
				
				this.classifierMap.put(lang, classifier);
			}
		} catch (Exception e) {
			LOG.fatal(e.getMessage());
		}
	}

	public Domain getDomain(String pageURL, String unicodeContent, Language contentLanguage) 
			throws UnsupportedDomainException
	{
		LOG.debug("== DOMAIN IDENTIFICATION STARTS ==");
		LOG.debug("URL: " + pageURL);
		LOG.debug("CONTENT: "  + unicodeContent);
		LOG.debug("LANGUAGE: " + contentLanguage);
		
		if (contentLanguage == Language.UNSUPPORTED)
			throw new UnsupportedDomainException();
		
		Classifier classifier = this.classifierMap.get(contentLanguage);
		Instances data = this.instancesMap.get(contentLanguage);
		Instance instance = new Instance(data.numAttributes());
        
		instance.setDataset(data);
		instance.setValue(0, unicodeContent);
		
		int ans = 0;
		double score = -100.0;
		String domain = "general";
		try {
			score = classifier.classifyInstance(instance);
			ans = (int) score;
			domain = data.classAttribute().value(ans);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			System.err.println(e.getMessage());
		}
		System.out.println("\n\nYeh Le------------"+pageURL+"\t"+score+"\n");
		
		LOG.debug("DOMAIN: " + domain);
		LOG.debug("== DOMAIN IDENTIFICATION ENDS ==");
		
		LOG.info("URL: " + pageURL + " classified as " + domain);
		
		if (domain.equalsIgnoreCase(Domain.TOURISM.toString())) {
			return Domain.TOURISM;
		}
		else if (domain.equalsIgnoreCase(Domain.HEALTH.toString()))
			return Domain.HEALTH;
		else 
			throw new UnsupportedDomainException();
	}

	public static DomainIdentifier getInstance() {
		if (domainIdentifier == null)
			domainIdentifier = new DomainIdentifier(NutchConfiguration.create());

		return domainIdentifier;
	}
	
	public static DomainIdentifier getInstance(Configuration conf) {
		if (domainIdentifier == null)
			domainIdentifier = new DomainIdentifier(conf);
		
		return domainIdentifier;
	}
	
	public static void main(String[] args) throws IOException 
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Put Text Here...");
		String text = br.readLine();
		
		System.out.println("URL of page");
		String url = br.readLine();
		
		DomainIdentifier domainIdnetifier = getInstance();
		
		LanguageIdentifier langIdentifier = LanguageIdentifier.getInstance();
		Language language = null;

		try {
			language = langIdentifier.getLanguage(url, text);
			System.out.println("Identified Language: " + language);

			String domain = domainIdentifier.getDomain(url, text, language).toString();
			System.out.println("Identified Domain: " + domain);
		} catch (UnsupportedLanguageException e) {
			e.printStackTrace();
		} catch (UnsupportedDomainException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	}




