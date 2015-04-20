package org.apache.nutch.tools;

import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;


public class URLInfo {
	public URLInfo(Text url, CrawlDatum datum, Inlinks linkInfo) {
		super();
		this.url = url;
		this.datum = datum;
	}
	public Text url;
	public CrawlDatum datum;
	public Inlinks linkInfo;
	
	
}
