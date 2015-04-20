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

package org.apache.nutch.crawl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.parse.ParseSegment;
import org.apache.nutch.tools.CrawlDBScanner;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Commons Logging imports

public class Crawl extends Configured implements Tool {
	public static ArrayList<URLDetails> urlSet = new ArrayList<URLDetails>();
	public static final Logger LOG = LoggerFactory.getLogger(Crawl.class);
	public static long TOPN = 1000;
	private static String getDate() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System
				.currentTimeMillis()));
	}

	/*
	 * Perform complete crawling and indexing (to Solr) given a set of root urls
	 * and the -solr parameter respectively. More information and Usage
	 * parameters can be found below.
	 */
	public static void main(String args[]) throws Exception {
		Configuration conf = NutchConfiguration.create();
		int res = ToolRunner.run(conf, new Crawl(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length < 1) {
			System.out
					.println("Usage: Crawl <urlDir> -solr <solrURL> [-dir d] [-threads n] [-depth i] [-topN N]");
			return -1;
		}
		Path rootUrlDir = null;
		Path dir = new Path("crawl-" + getDate());
		int threads = getConf().getInt("fetcher.threads.fetch", 10);
		int depth = 5;
		long topN = TOPN;
		String solrUrl = null;

		for (int i = 0; i < args.length; i++) {
			if ("-dir".equals(args[i])) {
				dir = new Path(args[i + 1]);
				i++;
			} else if ("-threads".equals(args[i])) {
				threads = Integer.parseInt(args[i + 1]);
				i++;
			} else if ("-depth".equals(args[i])) {
				depth = Integer.parseInt(args[i + 1]);
				i++;
			} else if ("-topN".equals(args[i])) {
				topN = Integer.parseInt(args[i + 1]);
				TOPN = topN;
				i++;
			} else if ("-solr".equals(args[i])) {
				solrUrl = args[i + 1];
				i++;
			} else if (args[i] != null) {
				rootUrlDir = new Path(args[i]);
			}
		}
//		topN = TOPN;
		JobConf job = new NutchJob(getConf());

		if (solrUrl == null) {
			LOG.warn("solrUrl is not set, indexing will be skipped...");
		} else {
			// for simplicity assume that SOLR is used
			// and pass its URL via conf
			getConf().set("solr.server.url", solrUrl);
		}

		FileSystem fs = FileSystem.get(job);

		if (LOG.isInfoEnabled()) {
			LOG.info("crawl started in: " + dir);
			LOG.info("rootUrlDir = " + rootUrlDir);
			LOG.info("threads = " + threads);
			LOG.info("depth = " + depth);
			LOG.info("solrUrl=" + solrUrl);
			if (topN != Long.MAX_VALUE)
				LOG.info("topN = " + topN);
		}

		/**
		 * @author swapnil
		 * **/
		//Initialize Base Set
		String trainingFile = getConf().get("training_file");
		StringBuffer buffer = new StringBuffer();
		StringBuffer outBuffer = new StringBuffer();
		Set<URLDetails> set = BaseSetLoader.updatePosBaseSetUsingDepth(getConf());
		buffer = BaseSetLoader.updateNegBaseSetUsingDepth(getConf());
		for (URLDetails urlDetails : set) {
			buffer.append("1 "+urlDetails.getFeatureVector());
			buffer.append("\n");
			
		}
		appendTrainingData(buffer.toString(),trainingFile);
		/*System.out.println("-------------------------------------------------");
		System.out.println(URLDetails.posUrlDetailBase);
		System.out.println("-------------------------------------------------");
		System.out.println(URLDetails.negUrlDetailBase);
		System.out.println("-------------------------------------------------");*/
		Classifier classifier = new Classifier(getConf());
		classifier.learn(trainingFile);
		//Initialize classifier
		/***
		 * changes end
		 * ***/
		
		Path crawlDb = new Path(dir + "/crawldb");
		Path linkDb = new Path(dir + "/linkdb");
		Path segments = new Path(dir + "/segments");
		Path indexes = new Path(dir + "/indexes");
		Path index = new Path(dir + "/index");
		Path prioritizeDb = new Path(dir + "/prioritizer");

		Path tmpDir = job.getLocalPath("crawl" + Path.SEPARATOR + getDate());
		Injector injector = new Injector(getConf());
		Generator generator = new Generator(getConf());
		Fetcher fetcher = new Fetcher(getConf());
		ParseSegment parseSegment = new ParseSegment(getConf());
		CrawlDb crawlDbTool = new CrawlDb(getConf());
		CrawlDbReader crawlDbReader = new CrawlDbReader(); 
		LinkDb linkDbTool = new LinkDb(getConf());
		Prioritizer prioritizer = new Prioritizer();
		CrawlDBScanner cbds = new CrawlDBScanner(NutchConfiguration.create()); 
		CrawlDatum cd = new CrawlDatum();
		
		// initialize crawlDb
		injector.inject(crawlDb, rootUrlDir);
		
		for(int i=0;i<depth ;i++) { // generate new segment
			if (i == -1) {
				break;
			}
			Path[] segs = generator.generate(crawlDb, segments, -1, topN,
					System.currentTimeMillis());
			
			if (segs == null) {
				LOG.info("Continuing From depth=" + i
						+ " - no more URLs to fetch.");
//				i = 0;
				continue;
			}
			fetcher.fetch(segs[0], threads); // fetch it
			if (!Fetcher.isParsing(job)) {
				parseSegment.parse(segs[0]); // parse it, if needed
			}
			crawlDbTool.update(crawlDb, segs, true, true); // update crawldb
//			if (i > 0) {
				linkDbTool.invert(linkDb, segments, true, true, false); // invert
																		// links

//				if (solrUrl != null) {
//					// index, dedup & merge
//					FileStatus[] fstats = fs.listStatus(segments,
//							HadoopFSUtil.getPassDirectoriesFilter(fs));
//
//					IndexingJob indexer = new IndexingJob(getConf());
//					indexer.index(crawlDb, linkDb,
//							Arrays.asList(HadoopFSUtil.getPaths(fstats)));
//
//					SolrDeleteDuplicates dedup = new SolrDeleteDuplicates();
//					dedup.setConf(getConf());
//					dedup.dedup(solrUrl);
//				}
			buffer = new StringBuffer();
			outBuffer = new StringBuffer();
			HashSet<String> res = cbds.run2(new String[]{crawlDb.toString(),prioritizeDb.toString(),"http://en\\.wikipedia\\.org/wiki/.*","-s","db_unfetched","-text"});
//			Iterator<Text> iter = res.iterator();
			System.out.println("My Size is " + res.size());
			prioritizer.init(getConf(), linkDb);
			classifier.startClassifier();
			for (String text : res) {
				URLDetails urlDetails = prioritizer.getURLDetails(text, crawlDb.toString());
				if(urlDetails==null) {
					System.out.println("N0 information on link : "+text);
					continue;
				}
				urlDetails.setScores();
				/*float score = */
				
				if(i!=0) {
					//FIXME call classifier here
					//get score and set it in url details
					//FIXME ask rahul about how to create a C++ server for SVM
//					classifier.classify(getConf().get("test_file"));
					urlDetails.calculateFinalScore(classifier);
				}else {
					urlDetails.setFinalScore(1.0f);
				}
				
				if(urlSet.contains(urlDetails)) {
					urlSet.remove(urlDetails);
				}
				urlSet.add(urlDetails);
				
				if(urlDetails.getFinalScore()>=0) {
					
				}else {
					buffer.append("\n");
					buffer.append("-1 "+urlDetails.getFeatureVector());
					outBuffer.append("\n");
					outBuffer.append("-1 "+urlDetails.getFeatureVector()+"\t"+urlDetails.getUrl()+" - "+urlDetails.getAnchorTextWords()+" "+urlDetails.getFinalScore());
				}
//				if(urlSet.contains(urlDetails)) {
//					urlSet.remove(urlDetails);
//				}
//				urlSet.add(urlDetails);
				
//				crawlDbReader.readUrlDatum(crawlDb.toString(), text, getConf()).setScore(score);;
				System.out.println("I am lucky ---------- " + urlDetails);
				
			}
			classifier.classify("Shutdown");
			Collections.sort(urlSet);
			for (int j = 0; j < TOPN && j<urlSet.size(); j++) {
				URLDetails details = urlSet.get(j);
				if (details.getFinalScore() >= 0) {
					buffer.append("\n");
					buffer.append("1 " + details.getFeatureVector());
					outBuffer.append("\n");
					outBuffer.append("1 "+details.getFeatureVector()+"\t"+details.getUrl()+" - "+details.getAnchorTextWords()+" "+details.getFinalScore());
				}else {
					break;
				}
			}
//			System.out.println("--------------------------------");
//			System.out.println(urlSet);
//			System.out.println("--------------------------------");
//			} else {
//				LOG.warn("No URLs to fetch - check your seed list and URL filters.");
//			}
			
			if(i==0) {
				//FIXME Rohit please add your code here				
				//learn on new set
				classifier.learn(trainingFile);
			}else {
				//learn on new set
				appendTrainingData(buffer.toString(),trainingFile);
				classifier.incLearn(trainingFile);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("crawl finished: " + dir);
			}
			appendOutputToFile(outBuffer.toString(),getConf().get("outputFolder")+"/"+i);
		}
		return 0;
	}
	
	public void appendTrainingData(String out, String trainingFile){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(trainingFile,true));
			writer.write(out);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void appendOutputToFile(String out, String file){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
			writer.write(out);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
