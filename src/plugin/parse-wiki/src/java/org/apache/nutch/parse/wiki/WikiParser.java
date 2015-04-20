package org.apache.nutch.parse.wiki;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.parse.Parser;
import org.apache.nutch.parse.ParserFactory;
import org.apache.nutch.protocol.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiParser implements Parser {
	public static final String CHARSET_UTF8 = "charset=UTF-8";

	public static final Logger LOG = LoggerFactory.getLogger(WikiParser.class);

	private Configuration conf;

	private ParserFactory parserFactory;

	private URLNormalizers normalizers;

	private URLFilters filters;

	private String defaultEncoding;

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
		this.parserFactory = new ParserFactory(conf);
		this.normalizers = new URLNormalizers(conf,
				URLNormalizers.SCOPE_OUTLINK);
		this.filters = new URLFilters(conf);
		this.defaultEncoding = conf.get("parser.character.encoding.default",
				"utf-8");
	}

	@Override
	public ParseResult getParse(Content content) {
		System.out.println("In Wiki Parser!!");
		ParseResult parseResult = new ParseResult(content.getUrl());

		String resultText = null;
		String resultTitle = null;
		Outlink[] outlinks = null;
		List<Outlink> outLinksList = new ArrayList<Outlink>();

		final String contentLen = content.getMetadata().get(
				Response.CONTENT_LENGTH);
		final int len = Integer.parseInt(contentLen);
		if (LOG.isDebugEnabled()) {
			LOG.debug("bziplen: " + len);
		}
		final byte[] contentInBytes = content.getContent();

		if (contentLen != null && contentInBytes.length != len) {
			return new ParseStatus(
					ParseStatus.FAILED,
					ParseStatus.FAILED_TRUNCATED,
					"Content truncated at "
							+ contentInBytes.length
							+ " bytes. Parser can't handle incomplete bzip file.")
					.getEmptyParseResult(content.getUrl(), getConf());
		}

		WikiExtractor extractor = new WikiExtractor(getConf());

		// extract text
		resultText = extractor.extractText(content.getUrl(),content);

		parseResult.put(content.getUrl(), new ParseText(""), new ParseData(
				new ParseStatus(ParseStatus.SUCCESS), resultText,
				new Outlink[0], content.getMetadata()));

		return parseResult;

	}
}
