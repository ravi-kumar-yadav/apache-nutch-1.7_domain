package org.apache.nutch.protocol.wiki;

import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.ProtocolOutput;
import org.apache.nutch.protocol.ProtocolStatus;
import org.apache.nutch.protocol.RobotRulesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.robots.BaseRobotRules;

public class Wiki implements Protocol {

	public static final Logger LOG = LoggerFactory.getLogger(Wiki.class);
	private Configuration conf;

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;

	}

	@Override
	public ProtocolOutput getProtocolOutput(Text url, CrawlDatum datum) {
		 String urlString = url.toString();
		    try {
		      URL u = new URL(urlString);
		      Content content = new Content(u.toExternalForm(), u.toExternalForm(), "<html><body>This is a Wiki dump</body></html>".getBytes(), "application/x-bzip2", new Metadata(), getConf());
		      return new ProtocolOutput(content,ProtocolStatus.STATUS_SUCCESS); 
		    } catch (Exception e) {
		      e.printStackTrace();
		      return new ProtocolOutput(null, new ProtocolStatus(e));
		    }
	}

	@Override
	public BaseRobotRules getRobotRules(Text url, CrawlDatum datum) {
		return RobotRulesParser.EMPTY_RULES;
	}

}
