package com.cs4274.news_butler.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.util.Version;

import com.cs4274.news_butler.util.ByValueComparator;
import com.cs4274.news_butler.util.ReverseComparator;


public class IndexSources {
	private static File indexDir;
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	private static final float topTermCutoff = 0;
	private static Analyzer analyzer; 
	private static boolean create = true;


	public IndexSources(String fileDir) {}
	
	
	public static void createIndex(String fileDir, String suffix)throws Exception {
		System.out.println("The file directory is :" +fileDir);
		
		indexDir = new File(fileDir+"/index/");
		
		System.out.println("The INDEX directory is :" + indexDir);
		if (!indexDir.exists())
			indexDir.mkdirs();
		
		analyzer = new EnglishAnalyzer(Version.LUCENE_36);
		//analyzer = new SnowballAnalyzer(Version.LUCENE_CURRENT,"English", importStopWords());
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
		if (create){
			config.setOpenMode(OpenMode.CREATE);
			create = false;
		}
		else {
			config.setOpenMode(OpenMode.APPEND);
		}
		
		IndexWriter indexWriter = new IndexWriter(
				FSDirectory.open(indexDir),
				config);
		
		File dir = new File(fileDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (!file.isDirectory() && file.exists() && file.canRead() 
					&& file.length() > 0.0 && file.isFile() && file.getName().endsWith(suffix)) {
				
				Document document = new Document();
	
				document.add(new Field("filename",file.getName(), Field.Store.YES, Field.Index.ANALYZED));
				document.add(new Field("content", new FileReader(file)));
				//document.add(new Field("text",path, Field.Store.YES, Index.ANALYZED));
									
				if (document != null)
					indexWriter.addDocument(document);
			}
		}
		
		indexWriter.close();
	}
	/*
	private static CharArraySet importStopWords() {
		int length = SMART_STOP_WORDS.length;
		CharArraySet stopWords = new CharArraySet(Version.LUCENE_CURRENT,length, false);
		
		System.out.println("Number of stop words:" + length);
		
		for (int i=0;i<length;i++) {
			stopWords.add(SMART_STOP_WORDS[i]);
		}
		return stopWords;
		
	}
	*/
	public static void computeTopTermQuery() throws Exception {
		  Directory directory = FSDirectory.open(indexDir); 
		  
		  final Map<String,Integer> frequencyMap = 
		      new HashMap<String,Integer>();
		    List<String> termlist = new ArrayList<String>();
		    IndexReader reader = IndexReader.open(directory);
		    TermEnum terms = reader.terms();
		    while (terms.next()) {
		      Term term = terms.term();
		      String termText = term.text();
		      int frequency = reader.docFreq(term);
		      frequencyMap.put(termText, frequency);
		      termlist.add(termText);
		    }
		    reader.close();
		    
		    // sort the term map by frequency descending
		    Collections.sort(termlist, new ReverseComparator<String>(
		      new ByValueComparator<String,Integer>(frequencyMap)));
		    // retrieve the top terms based on topTermCutoff
		    List<String> topTerms = new ArrayList<String>();
		    float topFreq = -1.0F;
		    for (String term : termlist) {
		      if (topFreq < 0.0F) {
		        // first term, capture the value
		        topFreq = (float) frequencyMap.get(term);
		        topTerms.add(term);
		      } else {
		        // not the first term, compute the ratio and discard if below
		        // topTermCutoff score
		        float ratio = (float) ((float) frequencyMap.get(term) / topFreq);
		        if (ratio >= topTermCutoff) {
		          topTerms.add(term);
		        } else {
		          break;
		        }
		      }
		    }
		    StringBuilder termBuf = new StringBuilder();
		 
		    for (String topTerm : topTerms) {
		      termBuf.append(topTerm).
		        append("(").
		        append(frequencyMap.get(topTerm)).
		        append(");");
		      
		    }
		    
		    exportResult(termBuf);
		    
		    //System.out.println(">>> top terms: " + termBuf.toString());
		    
		  }
	/*
	private static String SMART_STOP_WORDS[] =
		{
		   "a",
		   "able",
		   "about",
		   "they",
		   "think",
		   "third",
		   "this",
		   "thorough",
		   "thoroughly",
		   "those",
		   "though",
		   "three",
		   "through",
		   "throughout",
		   "thru",
		   "thus",
		   "to",
		   "together",
		   "too",
		   "took",
		   "toward",
		   "towards",
		   "tried",
		   "tries",
		   "truly",
		   "try",
		   "trying",
		   "twice",
		   "two",
		   "u",
		   "un",
		   "upon",
		   "us",
		   "use",
		   "used",
		   "useful",
		   "uses",
		   "using",
		   "usually",
		   "uucp",
		   "v",
		   "value",
		   "various",
		   "very",
		   "via",
		   "viz",
		   "vs",
		   "w",
		   "want",
		   "wants",
		   "was",
		   "way",
		   "we",
		   "welcome",
		   "well",
		   "went",
		   "were",
		   "what",
		   "whatever",
		   "when",
		   "whence",
		   "whoever",
		   "whole",
		   "whom",
		   "whose",
		   "why",
		   "will",
		   "willing",
		   "wish",
		   "with",
		   "within",
		   "without",
		   "wonder",
		   "would",
		   "x",
		   "y",
		   "yes",
		   "yet",
		   "you",
		   "your",
		   "z",
		   "zero"
		};
	*/
	private static void exportResult(StringBuilder termBuf) {		
		 try {
			 File exportFile = new File(indexDir,"results.txt");
             FileOutputStream fos = new FileOutputStream(exportFile);
		
             fos.write(termBuf.toString().getBytes());                
             fos.close();
         } catch (IOException e) {
             e.printStackTrace();
         }

	}
	
}