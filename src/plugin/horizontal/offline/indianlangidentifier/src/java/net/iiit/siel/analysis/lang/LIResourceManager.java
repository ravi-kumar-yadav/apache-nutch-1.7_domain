package net.iiit.siel.analysis.lang;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;

public class LIResourceManager
{
	private static final String RESOURCE_DIR_CONF_PROPERTY = "resources.indianlangidentifier.dir";
	private static final String LANGUAGE_MAPPINGS_FILENAME = "LanguageMappings.properties";
	
	private String diResourceDirPath;
	
	private final String ngpExt = ".ngp";
	
	private LIResourceManager() {
		
	}

	public void setConf(Configuration conf) {
		this.diResourceDirPath = conf.get(RESOURCE_DIR_CONF_PROPERTY);
	}
	
	public String getResourceDir() throws RuntimeException {
		if (this.diResourceDirPath == null)
			throw new RuntimeException("Configuration Property not set. Please set " + RESOURCE_DIR_CONF_PROPERTY + " property in nutch-site.xml to the Domain Identifier resources directory else contact IIIT-H. ");
		
		File diResDir = new File(this.diResourceDirPath);
		
		if (!diResDir.exists())
			throw new RuntimeException("Configuration Property " + RESOURCE_DIR_CONF_PROPERTY + " set to " + this.diResourceDirPath + ". Directory doesn't not exist. Set to the correct path or contact IIIT-H.");
		
		return this.diResourceDirPath;
	}
	
	public String getLanguageMappingsFilePath() throws RuntimeException {
		
		File langMapFile = new File(this.getResourceDir(), LANGUAGE_MAPPINGS_FILENAME);
		
		if (!langMapFile.exists())
			throw new RuntimeException("Resource Missing. Cannot find file " + langMapFile.getAbsolutePath() + ". The required Language Mappings file is missing. Contact IIIT-H");
		
		return langMapFile.getAbsolutePath();
	}
	
	public String getNgpPath(Language lang) throws RuntimeException {
		String modelFilePath;
		
		String langStr = lang.toString().toLowerCase();
		
		String modelFileName = langStr + ngpExt;
		
		File modelFile = new File(this.getResourceDir(), modelFileName);
		modelFilePath = modelFile.getAbsolutePath();
		
		if (!modelFile.exists())
			throw new RuntimeException("Resource Missing. Cannot find file " + modelFilePath + ". The required ngp file for language " + lang + " is missing. Contact IIIT-H");
		
		return modelFilePath;
	}
	
	private static LIResourceManager resourceManager = null;
	public static LIResourceManager getInstance(Configuration conf) {
		if (resourceManager == null)
			resourceManager = new LIResourceManager();
		
		resourceManager.setConf(conf);
		
		return resourceManager;
	}
	
	public static void main(String[] args) {
		Configuration conf = NutchConfiguration.create();
		
		LIResourceManager rm = LIResourceManager.getInstance(conf);
		
		System.out.println("Checking for configuration property: " + RESOURCE_DIR_CONF_PROPERTY);
		try {
			System.out.println(RESOURCE_DIR_CONF_PROPERTY + " set to " + rm.getResourceDir());
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
		
		System.out.println("Checking for Language Mappings File: " + LANGUAGE_MAPPINGS_FILENAME);
		try {
			System.out.println("File found at " + rm.getLanguageMappingsFilePath());
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
		
		for (Language lang : Language.values()) {
			if (lang == Language.UNSUPPORTED)
				continue;
			
			System.out.println("Checking ngp file of " + lang);
			try {
				System.out.println("File found at " + rm.getNgpPath(lang));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}	
	}
}
