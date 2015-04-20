/**
 * 
 */
package net.iiit.siel.analysis.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author niket
 * 
 */
public class TextLangMarker {

	private StringBuffer theText;
	public HashMap<String, ArrayList<String>> langRangeMarkerTable;
	public static String separator = "-";

	/**
	 * @return the theText
	 */
	public StringBuffer getTheText() {
		return theText;
	}

	/**
	 * @param theText
	 *            the theText to set
	 */
	public void setTheText(StringBuffer theText) {
		this.theText = theText;
	}

	/**
	 * @return the langRangeMarkerTable
	 */
	public HashMap<String, ArrayList<String>> getLangRangeMarkerTable() {
		return langRangeMarkerTable;
	}

	/**
	 * @param langRangeMarkerTable
	 *            the langRangeMarkerTable to set
	 */
	public void setLangRangeMarkerTable(
			HashMap<String, ArrayList<String>> langRangeMarkerTable) {
		this.langRangeMarkerTable = langRangeMarkerTable;
	}

	public TextLangMarker() {
		langRangeMarkerTable = new HashMap<String, ArrayList<String>>();
		this.theText = new StringBuffer("");
	}

	public TextLangMarker(String theText) {
		langRangeMarkerTable = new HashMap<String, ArrayList<String>>();

		this.theText = new StringBuffer(theText);
	}

	public TextLangMarker(StringBuffer theText) {
		langRangeMarkerTable = new HashMap<String, ArrayList<String>>();

		this.theText = new StringBuffer();
		this.theText = theText;
	}

	public StringBuffer getLangCharacters(String LangID) {
		ArrayList<String> rangeOfLangID = langRangeMarkerTable.get(LangID);
		StringBuffer textOfLangID = new StringBuffer(" ");
		int START_INDEX = 0;
		int END_INDEX = 1;

		for (Iterator<String> iterator = rangeOfLangID.iterator(); iterator
				.hasNext();) {
			String chunkOfTextOfLangID = (String) iterator.next();
			String[] startAndEndRange = chunkOfTextOfLangID.trim().split(
					separator);
			int start = 0;
			int end = 0;
			chunkOfTextOfLangID = "";

			/*
			 * Convert the range to int.
			 */
			start = Integer.parseInt(startAndEndRange[START_INDEX].trim());
			end = Integer.parseInt(startAndEndRange[END_INDEX].trim());

			//System.out.println("Start = " + start + "\tend= " + end);
			/*
			 * Fill the value through a function into chunkOfTextOfLangID
			 * because
			 */
			chunkOfTextOfLangID = computeChunkOfTextOfLangID(start, end);
			//System.out.println("ChunkOfTextOfLangID = " + chunkOfTextOfLangID);
			textOfLangID.append(chunkOfTextOfLangID);
			chunkOfTextOfLangID = "";
		}

		return textOfLangID;

	}

	/*
	 * Niket: The exceptionally larger texts are ignored in our analysis. A
	 * total of around 2 billion characters string is handled here. And we donot
	 * suppose the contents of a web page to be more than that.
	 */
	private String computeChunkOfTextOfLangID(int start, int end) {
		return theText.substring(start, end);
	}

	/**
	 * A utility function to be used while composing the the key value pairs
	 * with single character.
	 * 
	 * @param singleIndex
	 *            - e.g. a single character index needs to be inserted in index
	 *            range table.
	 */
	public String setLangRangeMarkerTable(int singleIndex) {
		String composedString = "";
		composedString = Integer.toString(singleIndex) + separator
				+ Integer.toString(singleIndex);
		return composedString;

	}

	/**
	 * A utility function to be used while composing the the key value pairs
	 * with single character till the end.
	 * 
	 * @param singleIndex
	 *            - e.g. a single character index needs to be inserted in index
	 *            range table.
	 */
	public String setLangRangeMarkerTableTillTheEnd(int singleIndex) {
		String composedString = "";
		composedString = Integer.toString(singleIndex) + separator
				+ Integer.toString(theText.length());
		return composedString;

	}

	/**
	 * A utility function to be used while composing the the key value pairs.
	 * 
	 * @param startIndex
	 *            - e.g. multiple characters index needs to be inserted in index
	 *            range table.
	 * @param endIndex
	 *            - e.g. multiple characters index needs to be inserted in index
	 *            range table.
	 */
	public String setLangRangeMarkerTable(int startIndex, int endIndex) {
		String composedString = "";
		composedString = Integer.toString(startIndex) + separator
				+ Integer.toString(endIndex);
		return composedString;

	}

	public void printHashmapTableWithFormatting() {
		String lotsOfDashes = "------------------------------------------------------------";
		System.out.println("\n\n\n" + lotsOfDashes
				+ "\nThe table of langID Range is as follows:" + lotsOfDashes
				+ "\n\n");
		ArrayList<String> tempRanges = new ArrayList<String>();

		for (Iterator<String> iterator = langRangeMarkerTable.keySet()
				.iterator(); iterator.hasNext();) {
			String langID = (String) iterator.next();
			System.out.println("\n\n\n" + langID + "\t\t");
			tempRanges = getLangRangeMarkerTable().get(langID);
			/*
			 * Now print the ranges.
			 */
			for (Iterator<String> iterator2 = tempRanges.iterator(); iterator2
					.hasNext();) {
				String range = (String) iterator2.next();
				System.out.print("\t" + range);
			}

		}

		System.out.println("\n\n\n" + lotsOfDashes + "\n\n");

	}

	public void putOneElementInTable(String langID,
			ArrayList<String> rangeUpdate) {
			langRangeMarkerTable.put(langID, rangeUpdate);

		}
	

}
