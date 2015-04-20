package net.iiit.siel.analysis.domain;

// Commons Logging imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Nutch imports
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;

// Hadoop imports
import org.apache.hadoop.conf.Configuration;

// DOM imports
import org.w3c.dom.DocumentFragment;


public class DomainParserFilter implements HtmlParseFilter {
  
	public static final Log LOG = LogFactory.getLog(DomainParserFilter.class);
	private Configuration conf;

	//private Configuration conf;
  
	public ParseResult filter(Content content, ParseResult parse, HTMLMetaTags metaTags, DocumentFragment doc) {
		return parse;
	}
 
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}
}
