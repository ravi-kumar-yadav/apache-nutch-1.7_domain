package net.iiit.siel.analysis.domain;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class CrossValidateARFF 
{
	private static DecimalFormat df = new DecimalFormat("#.##");
	public static void main(String args[]) throws Exception 
	{
		if (args.length != 1 && args.length != 2) {
			System.out.println("USAGE: CrossValidateARFF <arff_file> [<stop_words_file>]");
			System.exit(-1);
		}
		
		String arffFilePath = args[0];

		DataSource ds = new DataSource(arffFilePath);
		Instances data = ds.getDataSet();

		// setting class attribute
		data.setClassIndex(data.numAttributes() - 1);
		
		
		NaiveBayes naiveBayes = new NaiveBayes();
		StringToWordVector stwv=new StringToWordVector();
		stwv.setInputFormat(data);
		stwv.setLowerCaseTokens(true);
		
		if (args.length == 2) {
			String stopwordsFilePath = args[1];
			stwv.setStopwords(new File(stopwordsFilePath));
		}
		
		Instances filteredData = Filter.useFilter(data, stwv);
		
		
		Evaluation eval = new Evaluation(filteredData);
		eval.crossValidateModel(naiveBayes, filteredData, 10, new Random(1));
		
		System.out.println("Correctly classified instances:\t"+eval.correct());
		System.out.println("No. of incorrectly classified instances:\t" + eval.incorrect());
		System.out.println("Percentage of correctly classified instances:\t"+ df.format(eval.pctCorrect()) +"\n");

		double[][] confusionMatrix = eval.confusionMatrix();

		int numClasses = data.classAttribute().numValues();

		for (int i = 0; i < numClasses; i++)
			System.out.print(data.classAttribute().value(i) + "\t");
		System.out.println("Precision\tRecall\tF-Score");

		for (int i=0; i < numClasses; i++) {
			for (int j=0; j < numClasses; j++) {
				System.out.print((int)confusionMatrix[i][j] + "\t");
			}
			System.out.print(df.format(eval.precision(i)) + "\t" + df.format(eval.recall(i)) + "\t" + df.format(eval.fMeasure(i)) + "\t");
			System.out.println("<== " + data.classAttribute().value(i));
		}
	}
}															
