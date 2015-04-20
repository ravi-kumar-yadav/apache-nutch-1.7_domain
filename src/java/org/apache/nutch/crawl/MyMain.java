package org.apache.nutch.crawl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.nutch.util.NutchConfiguration;

public class MyMain {

	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("link4/url.txt"));
		String line= null;
		Prioritizer prioritizer = new Prioritizer();
		prioritizer.init(NutchConfiguration.create(), new Path("/home/swapnil/WWWworkspace/apache-nutch-1.7/crawl4/linkdb"));
		while((line=reader.readLine())!=null) {
			prioritizer.getURLDetails(line,"/home/swapnil/WWWworkspace/apache-nutch-1.7/crawl4/crawldb");
			
		}
	}

}
