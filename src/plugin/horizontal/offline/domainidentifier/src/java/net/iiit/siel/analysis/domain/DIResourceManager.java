package net.iiit.siel.analysis.domain;

import java.io.File;

import net.iiit.siel.analysis.lang.Language;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;

public class DIResourceManager {
	public static final String RESOURCE_DIR_CONF_PROPERTY = "resources.domainidentifier.dir";
	
	private String diResourceDirPath;
	
	private final String arffExt = ".arff", domainSuffix = "_tourism_health_general", modelExt = ".model";
	
	private DIResourceManager() {
		
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
	
	public String getARFFPath(Language lang) throws RuntimeException {
		String arffFilePath;
		
		String langStr = lang.toString().toLowerCase();
		
		String arffFileName = langStr + domainSuffix + arffExt;
		
		File arffFile = new File(this.getResourceDir(), arffFileName);
		arffFilePath = arffFile.getAbsolutePath();
		
		if (!arffFile.exists())
			throw new RuntimeException("Resource Missing. Cannot find file " + arffFilePath + ". The required ARFF file for language " + lang + " is missing. Contact respective LV and IIIT-H");
		
		return arffFilePath;
	}
	
	public String getModelPath(Language lang) throws RuntimeException {
		String modelFilePath;
		
		String langStr = lang.toString().toLowerCase();
		
		String modelFileName = langStr + domainSuffix + modelExt;
		
		File modelFile = new File(this.getResourceDir(), modelFileName);
		modelFilePath = modelFile.getAbsolutePath();
		
		if (!modelFile.exists())
			throw new RuntimeException("Resource Missing. Cannot find file " + modelFilePath + ". The required model file for language " + lang + " is missing. Contact IIIT-H");
		
		return modelFilePath;
	}
	
	private static DIResourceManager resourceManager = null;
	public static DIResourceManager getInstance(Configuration conf) {
		if (resourceManager == null)
			resourceManager = new DIResourceManager();
		
		resourceManager.setConf(conf);
		
		return resourceManager;
	}
	
	public static void main(String[] args) {
		Configuration conf = NutchConfiguration.create();
		
		DIResourceManager rm = DIResourceManager.getInstance(conf);
		
		System.out.println("Check for configuration property: " + RESOURCE_DIR_CONF_PROPERTY);
		System.out.println(RESOURCE_DIR_CONF_PROPERTY + " set to " + rm.getResourceDir());
		
		for (Language lang : Language.values()) {
			if (lang == Language.UNSUPPORTED)
				continue;
			try {
				System.out.println("Checking ARFF file of " + lang);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(0);
			}
			try {
				System.out.println("File found at " + rm.getARFFPath(lang));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
			System.out.println("Checking Model file of " + lang);
			try {
				System.out.println("File found at " + rm.getModelPath(lang));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}	
	}
}
