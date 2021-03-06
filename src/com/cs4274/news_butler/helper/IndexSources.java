package com.cs4274.news_butler.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.util.Version;

import com.cs4274.news_butler.util.ByValueComparator;
import com.cs4274.news_butler.util.ReverseComparator;
import com.cs4274.news_butler.util.StopWords;



public class IndexSources {
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "content";
	public static final String USER_INDEX = "USER_INDEX";
	public static final String ARTICLE_INDEX = "ARTICLE_INDEX";
	private static final float topTermCutoff = (float) 0.05;
	private static Analyzer analyzer; 


	public IndexSources(String fileDir) {}
	
	
	public static void createIndex(String appDir, String content)throws Exception {
		File indexDir;	
		Set<String> set = new HashSet<String>(Arrays.asList(StopWords.SMART_STOP_WORDS));
		analyzer = new EnglishAnalyzer(Version.LUCENE_36,set);
		
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);		
		
		// rebuild everytime when learning user�s preference (delete entire index folder)
			config.setOpenMode(OpenMode.CREATE);
			indexDir = new File(appDir + "/" + USER_INDEX + "/");
			
			if (indexDir.exists()) {
				delete(indexDir);
			} 
			else {
				indexDir.mkdir();
			}
			
			IndexWriter indexWriter = new IndexWriter(
					FSDirectory.open(indexDir),
					config);
							
					Document document = new Document();
					
					//document.add(new Field(FIELD_CONTENTS, new FileReader(file)));				
					document.add(new Field(FIELD_CONTENTS,content,Field.Store.YES, Field.Index.ANALYZED));
										
					if (document != null)
						indexWriter.addDocument(document);

			indexWriter.close();								
	}


	public static List<String> computeTopTermQuery(File indexDir) throws Exception {
		  Directory directory = FSDirectory.open(indexDir); 
		  
		  final Map<String,Integer> frequencyMap = 
		      new HashMap<String,Integer>();
		    List<String> termlist = new ArrayList<String>();
		    IndexReader reader = IndexReader.open(directory);
		    TermEnum terms = reader.terms();
		    while (terms.next()) {
		      Term term = terms.term();
		      String termText = term.text();
		      TermDocs td = reader.termDocs(new Term("content", termText));
		      int frequency = 0;
		      while (td.next()) {
		         frequency += td.freq();
		      }
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
		    
		    return topTerms;
		      
		     
		  }
	
	private static void delete(File indexDir) throws IOException{	
		if(indexDir.isDirectory()){
    		if(indexDir.list().length==0){
    		   indexDir.delete();
    		   
    		}else{
    		   //list all the directory contents
        	   String files[] = indexDir.list();
 
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(indexDir, temp);
        	      
        	      //recursive delete
        	     delete(fileDelete);
        	   }
 
        	   //check the directory again, if empty then delete it
        	   if(indexDir.list().length==0){
           	     indexDir.delete();
        	   }
    		}
 
    	}else{
    		//if file, then delete it
    		indexDir.delete();
    	}		
	}
	

}