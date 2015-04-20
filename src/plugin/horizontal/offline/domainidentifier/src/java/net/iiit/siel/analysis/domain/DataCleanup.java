package net.iiit.siel.analysis.domain;

/**
 * 
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author niket
 * 
 */
public class DataCleanup 
{
  /* ArrayList of Characters for a particular language. */
  private String BENGALI = "bn";
  private String ENGLISH = "en";
  private String HINDI = "hi";
  private String MARATHI = "mr";
  private String TELUGU = "te";
  private String TAMIL = "ta";
  private String GURUMUKHI = "pa";

  public ArrayList<StringBuffer> readFile(String filePath) 
  {
    StringBuffer fileContents = new StringBuffer("");
    ArrayList<StringBuffer> fileContentsComplete = new ArrayList<StringBuffer>();  
    String temp = "";
    try 
    {
      BufferedReader in = new BufferedReader(new FileReader(filePath));
      while ((temp = in.readLine()) != null) 
      {
	String UTF8Str = "";
	try 
        {
	  UTF8Str = new String(temp.getBytes(), "UTF-8");
	  fileContents.append(UTF8Str + "\n");
	} 
        catch (OutOfMemoryError e) 
        {
	  /* Store the fileContents in an array */
	  e.printStackTrace();
	  fileContentsComplete.add(fileContents);
	  fileContents = null;
	  fileContents = new StringBuffer(UTF8Str);
	  continue;
	} 
        catch (Exception e) 
        {
	  e.printStackTrace();
	  continue;
	}

      }
      
      in.close();

      // dispose all the resources after using them.

    }
    catch (FileNotFoundException e) 
    {
      e.printStackTrace();
    }
    catch (IOException e) 
    {
      e.printStackTrace();
    }

    if (fileContentsComplete.size() < 1) 
    {
      fileContentsComplete.add(fileContents);
    }
    return fileContentsComplete;
  }

  public static String returnCleanContents(String langID,String contentsToClean) 
  {
    DataCleanup object = new DataCleanup();
    String returnedContents = "";
    ArrayList<StringBuffer> cleanedFileContentList = new ArrayList<StringBuffer>();
    cleanedFileContentList = object.getCleanFile(langID, contentsToClean);
		
    /* Error handling. */
    if(cleanedFileContentList==null || cleanedFileContentList.size()<1)
    {
      if(cleanedFileContentList==null)
      {
	System.out.println("CleanedFileContentList is null");
      }
      else
      System.out.println("CleanedFileContentList.size = "+cleanedFileContentList.size());
      return returnedContents;
    }
		
    for (Iterator<StringBuffer> iterator = cleanedFileContentList.iterator(); iterator.hasNext();) 
    {
      StringBuffer cleanedFileContent = (StringBuffer) iterator.next();
      String spaceRegex = new String("(\\s\\s)+");
      String lineRegex = new String("(\\n\\n)+");
      returnedContents = (((cleanedFileContent.toString().replaceAll(spaceRegex, " ")).
                            replaceAll("\n \n", "\n")).replaceAll(lineRegex, "\n"));
    }
    return returnedContents;

  }

  private ArrayList<StringBuffer> getCleanFile(String langID,String cleanFileContentsString) 
  {
    StringBuffer cleanFileContents = new StringBuffer();
    ArrayList<StringBuffer> cleanFileList = new ArrayList<StringBuffer>();
    String tempFileContents = new String();
 
    Boolean includeChar = false;
    Character space = ' ';
    Character newLineFeed = '\n';

    /* Initialize tempFileContents with original File contents. */

    tempFileContents = cleanFileContentsString.toString();
    int INT_MAX_SIZE = Integer.MAX_VALUE;

    for (int i = 0; i < tempFileContents.length(); i++) 
    {
      includeChar = compareCharWithGivenLang(langID, tempFileContents.charAt(i));
      /* if yes, include char. else put a space. */
      if (includeChar) 
      {
	cleanFileContents.append(tempFileContents.charAt(i));
      } 
      else 
      {
	if (tempFileContents.charAt(i) == newLineFeed)
	  cleanFileContents.append(newLineFeed);
	else
	  cleanFileContents.append(space);
      }
	
      /* Chunk the string so that int value is not exceeded. */
      if (i == (INT_MAX_SIZE)) 
      {
	tempFileContents = tempFileContents.substring(i);
	i = 0;
      }
    }
    cleanFileList.add(cleanFileContents);
    cleanFileContents = null;

    return cleanFileList;
  }

	/**
	 * Preprocess to identify the language by simply using the character
	 * encoding
	 * 
	 * @param content
	 * @param index
	 * @return The enum containing shortname
	 */

  private Boolean compareCharWithGivenLang(String langID,Character characterToCheck) 
  {
    Boolean doesCharBelongToLangID = false;
    UnicodeBlock currentUnicode = Character.UnicodeBlock.of(characterToCheck);

    if (currentUnicode == Character.UnicodeBlock.BENGALI && langID.equalsIgnoreCase(BENGALI)) 
    {
      doesCharBelongToLangID = true;
    } 
    else if (currentUnicode == Character.UnicodeBlock.TELUGU && langID.equalsIgnoreCase(TELUGU)) 
    {
      doesCharBelongToLangID = true;
    } 
    else if (currentUnicode == Character.UnicodeBlock.TAMIL && langID.equalsIgnoreCase(TAMIL)) 
    {
      doesCharBelongToLangID = true;
    } 
    else if (currentUnicode == Character.UnicodeBlock.DEVANAGARI && (langID.equalsIgnoreCase(HINDI) || langID
					.equalsIgnoreCase(MARATHI))) 
    {
      doesCharBelongToLangID = true;
    } 
    else if (currentUnicode == Character.UnicodeBlock.GURMUKHI	&& langID.equalsIgnoreCase(GURUMUKHI)) 
    {
      doesCharBelongToLangID = true; 
    } 
    else if (langID.equalsIgnoreCase(ENGLISH)&& ((characterToCheck >= 65 && characterToCheck <= 90) ||                      (characterToCheck>=97 && characterToCheck <= 122))) 
    {
      doesCharBelongToLangID = true;
    } 
    else 
    {

			/*
			 * Nothing to do with english
			 */
			// System.out.println("into else loop");
    }

    return doesCharBelongToLangID;
 
  }

}
