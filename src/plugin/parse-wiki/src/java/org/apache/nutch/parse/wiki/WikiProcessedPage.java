/**
 * 
 */
package org.apache.nutch.parse.wiki;

import java.util.Vector;

/**
 * @author arjun
 *
 */
public class WikiProcessedPage {
	String url;
	String title;
	String content;
	Vector<String> outlinks;
	Vector<String> categories;

	public WikiProcessedPage(String url, String title, String content,
			Vector<String> outlinks, Vector<String> categories) {
		this.url =url;
		this.title = title;
		this.content=content;
		this.outlinks=outlinks;
		this.categories=categories;
	}

	
}
