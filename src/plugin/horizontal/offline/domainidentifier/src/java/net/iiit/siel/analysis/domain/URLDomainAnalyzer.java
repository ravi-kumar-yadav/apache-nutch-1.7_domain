package net.iiit.siel.analysis.domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;

public class URLDomainAnalyzer {
	
	HashSet<String> hm1,hm2,hm3;
	float score;
	float topicwords_count;
	float no_of_matched;
	URL url;
	public URLDomainAnalyzer(Configuration conf) {
		BufferedReader br1,br2,br3;
		String resourceDir = conf.get("resources.domainidentifier.dir");
	    
	    try {
			br1=new BufferedReader(new FileReader(new File(resourceDir+"/URL/trusted_tourism_domain_names.txt")));
			br2=new BufferedReader(new FileReader(new File(resourceDir+"/URL/positive_tourism_words.txt")));
			br3=new BufferedReader(new FileReader(new File(resourceDir+"/URL/url_stopwords.txt")));
			hm1=new HashSet();
			hm2=new HashSet();
			hm3=new HashSet();
			
			String ss1,ss2,ss3,ss4;
			while ((ss1=br1.readLine())!=null)
			{
				hm1.add(ss1);
			}
			while ((ss2=br2.readLine())!=null)
			{	  
			  hm2.add(ss2);	  
			}
			while ((ss3=br3.readLine())!=null)
			{	  
			  hm3.add(ss3);	  
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public double tourismScore(URL url) 
	{
		StringTokenizer st1;
		String ss3,ss4;
		ss3=url.toString();
		ss4=ss3;
		score=0;
		topicwords_count=0;
		no_of_matched=0;
		
		if (hm1.contains(url.getHost()))
		{	
		 score=score+1;	
		}
	   
	    ss3=ss3.replaceAll("[-/_.:%&~!?=+@#$1234567890]"," ");
		st1=new StringTokenizer(ss3," ");
		while (st1.hasMoreTokens())
		{	
		 String temp=st1.nextToken();
		 if (!hm3.contains(temp)&&(temp.length()>=3)&&(temp.length()<=15))
		 {	 
		   topicwords_count++;
		   if (hm2.contains(temp))
		   {	   
			 no_of_matched++;  
		   }
		 }
	    }
		if (no_of_matched/topicwords_count>=0.4)
		{	
		  return (no_of_matched/topicwords_count);	
		}
		else
		{	
		  score=score+(no_of_matched/topicwords_count);
		  return score/2;	
		}
	}
	
	public double healthScore(URL url) {
		// TODO: Add scoring code for health here
		return 0;
		
	}
}
