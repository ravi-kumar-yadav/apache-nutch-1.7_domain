package net.iiit.siel.analysis.domain;

import java.io.BufferedWriter;
import java.io.FileWriter;

import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;

public class GenerateARFF 
{
	public static void main(String args[]) throws Exception
	{
		if (args.length != 2)
		{
			System.out.println("USAGE:  GenerateARFF <data directory> <output arff file>");
			System.exit(-1);
		}
		
		String directoryTreePath = args[0];
		String arffFilePath = args[1];
		
		String[] options = {"-dir", directoryTreePath };
		
		TextDirectoryLoader tdl=new TextDirectoryLoader();
		tdl.setOptions(options);
		Instances data = tdl.getDataSet();
		
		data.setClassIndex(data.numAttributes() - 1);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(arffFilePath));
		writer.write(data.toString());
		writer.flush(); 
	    writer.close();
	}
}
