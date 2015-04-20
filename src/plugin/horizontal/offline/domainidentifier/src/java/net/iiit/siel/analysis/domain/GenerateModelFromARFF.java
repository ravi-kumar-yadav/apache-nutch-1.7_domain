package net.iiit.siel.analysis.domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class GenerateModelFromARFF
{
	public static void main(String args[]) throws Exception
	{
		if (args.length != 2 && args.length != 3)
		{
			System.out.println("USAGE:  GenerateModelFromARFF <arff_file> <model_file name> [<stop_words_file>]");
			System.exit(-1);
		}
	    String arffFilePath = args[0];
	    DataSource ds=new DataSource(arffFilePath);
	    Instances data=ds.getDataSet();
		if (data.classIndex()==-1)
		{
			data.setClassIndex(data.numAttributes() -1);
		}
		
		FilteredClassifier classifier=new FilteredClassifier();
	    
	    String modelFilePath = args[1];
	    StringToWordVector stwv=new StringToWordVector();
	    if (args.length == 3) {
	    	String stopwordsFile = args[2];
	    	stwv.setStopwords(new File(stopwordsFile));
	    }
	    
	    Classifier nbClassifier = new NaiveBayes();
	    
	    classifier.setFilter(stwv);
	    classifier.setClassifier(nbClassifier);
	    
	    classifier.buildClassifier(data);
	    
	    weka.core.SerializationHelper.write(modelFilePath, classifier);
		
	}
}
