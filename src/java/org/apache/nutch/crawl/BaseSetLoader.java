/**
 * 
 */
package org.apache.nutch.crawl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;

/**
 * @author swapnil
 * 
 */
public class BaseSetLoader {

	/**
	 * @param args
	 */
	public static void updatePosBaseSetUsingResource(Configuration configuration)
			throws Exception {
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("positive_set", "resources/positiveSet.txt")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			URLDetails details = new URLDetails(line);
			details.setURLTokens(line);
			details.getParentURLTokens().addAll(details.getUrlTokens());
			details.getAnchorTextWords().addAll(details.getUrlTokens());
			details.addParentScore(1.0f);
			URLDetails.addURLDetailsToPosStatic(details);
			// System.out.println(details);
		}

		reader.close();
	}

	public static void updateNegBaseSetUsingResource(Configuration configuration)
			throws Exception {
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("negative_set", "resources/negativeSet.txt")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			URLDetails details = new URLDetails(line);
			details.setURLTokens(line);
			details.getParentURLTokens().addAll(details.getUrlTokens());
			details.getAnchorTextWords().addAll(details.getUrlTokens());
			details.addParentScore(1.0f);
			URLDetails.addURLDetailsToNegStatic(details);
			System.out.println(details);
		}

		reader.close();
	}

	public static Set<URLDetails> updatePosBaseSetUsingDepth(Configuration configuration)
			throws Exception {
		Set<URLDetails> set = new HashSet<URLDetails>();
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("positive_set", "resources/positiveSet.txt")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.toLowerCase();
			line = line.trim();
			int ind = line.indexOf(" ");
			String url = line.substring(0, ind);
			URLDetails details = new URLDetails(url);
			line = line.substring(ind + 1);
			line = line.trim();
			ind = line.indexOf("]") + 1;
			line = line.substring(ind);
			line = line.trim();
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			String s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			String arr[] = s.split(",");
			details.setURLTokens(url);

			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getParentScores().addAll(arrayAsList(arr));
			
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getParentURLTokens().addAll(arrayAsSet(arr));
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getAnchorTextWords().addAll(arrayAsSet(arr));
			
			
			URLDetails.addURLDetailsToPosStatic(details);
			details.setFinalScore(-1.0f);
			set.add(details);
//			System.out.println(details);
		}

		reader.close();
		return set;
	}

	public static StringBuffer updateNegBaseSetUsingDepth(Configuration configuration)
			throws Exception {
		StringBuffer buffer = new StringBuffer();
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("negative_set", "resources/negativeSet.txt")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.toLowerCase();
			line = line.trim();
			int ind = line.indexOf(" ");
			String url = line.substring(0, ind);
			URLDetails details = new URLDetails(url);
			line = line.substring(ind + 1);
			line = line.trim();
			ind = line.indexOf("]") + 1;
			line = line.substring(ind);
			line = line.trim();
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			String s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			String arr[] = s.split(",");
			details.setURLTokens(url);

			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getParentScores().addAll(arrayAsList(arr));
			
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getParentURLTokens().addAll(arrayAsSet(arr));
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getAnchorTextWords().addAll(arrayAsSet(arr));
			
			URLDetails.addURLDetailsToNegStatic(details);
			details.setFinalScore(-1.0f);
			buffer.append("-1 "+details.getFeatureVector());
			buffer.append("\n");
//			System.out.println(details);
		}

		reader.close();
		return buffer;
	}

	public static ArrayList<Float> arrayAsList(String[] arr) {
		ArrayList<Float> list = new ArrayList<Float>();
		for (String string : arr) {
			string = string.trim();
			try {
				Float f = Float.parseFloat(string);
				list.add(f);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static HashSet<String> arrayAsSet(String[] arr) {
		HashSet<String> list = new HashSet<String>();
		for (String string : arr) {
			if("wiki".equalsIgnoreCase(string)) {
				continue;
			}
			string = string.trim();
			list.add(string);
		}

		return list;
	}
}
