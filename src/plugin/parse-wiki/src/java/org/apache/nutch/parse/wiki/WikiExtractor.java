package org.apache.nutch.parse.wiki;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.metadata.Feed;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.parse.Parser;
import org.apache.nutch.parse.ParserFactory;
import org.apache.nutch.parse.ParserNotFound;
import org.apache.nutch.protocol.Content;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class WikiExtractor {

	private ParserFactory parserFactory;

	private URLFilters filters;

	private URLNormalizers normalizers;

	private Configuration conf;

	public WikiExtractor(Configuration conf) {
		// TODO Auto-generated constructor stub
	}

	public String extractText(String url, final Content pageContent) {

		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(url);
		try {

			wxsp.setPageCallback(new PageCallbackHandler() {

				@Override
				public void process(WikiPage page) {
					// TODO Auto-generated method stub
					String title = page.getTitle();
					String url = "http://en.wikipedia.org/wiki/"
							+ title.replaceAll(" ", "\\s+")
									.replaceAll(" ", "_");
					String content = page.getText();
					Vector<String> outlinks = page.getLinks();
					Vector<String> categories = page.getCategories();

					WikiProcessedPage processedPage = new WikiProcessedPage(
							url, title, content, outlinks, categories);
					addToMap(new ParseResult(url), processedPage, pageContent);
				}
			});
			wxsp.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private void addToMap(ParseResult parseResult, WikiProcessedPage page,
			Content pageContent) {
		String link = page.url, text = page.content, title = page.title;
		Metadata parseMeta = new Metadata(), contentMeta = pageContent
				.getMetadata();
		Parse parse = null;
		try {
			link = normalizers.normalize(link, URLNormalizers.SCOPE_OUTLINK);

			if (link != null)
				link = filters.filter(link);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (link == null)
			return;

		parseMeta.set("TITLE", title);
		Outlink[] outlinks = null;
		String[] temp;
		int i = 0;
		for (String out : page.outlinks) {
			try {
				if ((temp = out.split("\\|\\|")).length > 1) {
					outlinks[i] = new Outlink("http://en.wikipedia.org/wiki/"
							+ temp[0].replaceAll(" ", "\\s+").replaceAll(" ", "_"),
							temp[1]);
				} else {
					outlinks[i] = new Outlink("http://en.wikipedia.org/wiki/"
							+ temp[0].replaceAll(" ", "\\s+").replaceAll(" ", "_"),temp[0]);
				}
				i++;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (String cat : page.categories)
			parseMeta.set("CATEGORY", cat);


		parseResult.put(link, new ParseText(text), new ParseData(
				ParseStatus.STATUS_SUCCESS, title, outlinks, contentMeta,
				parseMeta));

	}
}
