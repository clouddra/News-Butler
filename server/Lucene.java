

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;


public class Lucene {
	
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "content";
	public static final String ARTICLE_INDEX = "article_index";
	public static final String ARTICLES = "articles";
	public static final String RANKING = "ranking";
	private static final float topTermCutoff = (float) 0.1;
	private static Analyzer analyzer; 
	
	private static String applicationDirectory = getWorkingDirectory();
	public static final String suffix = ".txt";
	public static Set<String> hashedArticles = new HashSet<String>();

	public static void main(String[] args) throws Exception {
		 createIndex(applicationDirectory);
		 computeDomainTopTerms(applicationDirectory);	
		 System.out.println("Finished saving to Json");
	}


	/*
	*	Method to index articles, run this method first
	*/
	public static void createIndex(String applicationDirectory)throws Exception {
		File indexDir;
		String sourceDir;
		
		// create the article_index directory
		File articleIndex = new File(applicationDirectory+File.separator+ARTICLE_INDEX+File.separator);
		if (!articleIndex.exists())
			articleIndex.mkdir();	
		
		// /articles
		File dir = new File(applicationDirectory+File.separator+ARTICLES); 
		File[]  listDir =  dir.listFiles();
			
		// each domain will have its own index
		for (File eachDir : listDir) {	
			if (eachDir.isDirectory()) {
				String directoryName = eachDir.getName();								
				
				// /article_index/soccer_index
				indexDir = new File(applicationDirectory+File.separator+ ARTICLE_INDEX + File.separator + directoryName + "_index"+File.separator);									
					
				if (!indexDir.exists()) 
					indexDir.mkdirs();
													
				Set<String> set = new HashSet<String>(Arrays.asList(SMART_STOP_WORDS)); 
				analyzer = new EnglishAnalyzer(Version.LUCENE_36,set);
					
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);		
				// Only create the index for the first time and subsequent learning will update the index
				config.setOpenMode(OpenMode.CREATE_OR_APPEND);
					
				IndexWriter indexWriter = new IndexWriter(FSDirectory.open(indexDir), config);
				
				// /articles/soccer
				sourceDir = applicationDirectory+File.separator+ARTICLES+File.separator+directoryName+File.separator;
				System.out.println(sourceDir);
				File sdir = new File(sourceDir);
				File[] files = sdir.listFiles();
				for (File file : files) {
					if (!file.isDirectory() && file.exists() && file.canRead() 
							&& file.length() > 0.0 && file.isFile() && file.getName().endsWith(suffix)) {
						
						// only index files that are not indexed before
						if (hashedArticles.contains(file.getName()) == false) {
							hashedArticles.add(file.getName());
							
							Document document = new Document();
								
							document.add(new Field(FIELD_CONTENTS, new FileReader(file)));
							document.add(new Field("filename",file.getName(), Field.Store.YES, Field.Index.ANALYZED));
													
							if (document != null)
								indexWriter.addDocument(document);
						}
					}
				}
					
				indexWriter.close();
			}
		}				
	}
	
	/*
	 * Method to compute top terms for each domain AFTER indexing
	 */
	public static void computeDomainTopTerms (String applicationDirectory) {	
			List<Domain> topTerms = new ArrayList<Domain>();
			try {
					// create the ranking directory
					File ranking = new File(applicationDirectory+File.separator+RANKING+File.separator);
					if (!ranking.exists())
						ranking.mkdir();	
				
					// /article_index
					File indexDir = new File(applicationDirectory + File.separator + ARTICLE_INDEX + File.separator);
					String[] directory = indexDir.list();

					
					for (String eachDir : directory) {
						// /article_index/soccer_index
						
						File domainDir = new File(applicationDirectory + File.separator+ ARTICLE_INDEX + File.separator + eachDir + File.separator);
						List<String> articleTopTerms = computeTopTermQuery(domainDir);
						String domainName = eachDir.replace("_index", "");
						
						
						topTerms.add(new Domain(domainName, articleTopTerms.toArray(new String[articleTopTerms.size()])));
						
						saveToJson(domainName, articleTopTerms);
						
					}
					saveToJson(topTerms);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	}	

	
	
	/******************************************************************
	 * Helper methods
	 ******************************************************************/
	
	private static String getWorkingDirectory() {
		return System.getProperty("user.dir");	
	}
	
	public static void saveToJson(List<Domain> topTerms) throws IOException{
			
			/*
			OutputStream out =  new FileOutputStream(applicationDirectory+File.separator+RANKING+File.separator+domainName+".json");
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
			writer.setIndent("   ");
			writer.beginObject();
			writer.name("domain").value(domainName);
			writer.name("keywords");
				writer.beginArray();
				Iterator<String> itr = data.iterator();
				while (itr.hasNext())
					writer.value(itr.next());
				writer.endArray();
			writer.endObject();
			writer.close();
			*/
			PrintWriter  out =  new PrintWriter (applicationDirectory+File.separator+RANKING+File.separator+"articleTerms.json");
			Gson gson = new Gson();
			String json = gson.toJson(topTerms, new TypeToken<List<Domain>>() {}.getType());
			out.write(json);
			out.close();
			
		}	
	public static void saveToJson(String domainName, List<String> data) throws IOException{
		
		
		OutputStream out =  new FileOutputStream(applicationDirectory+File.separator+RANKING+File.separator+domainName+".json");
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("   ");
		writer.beginObject();
		writer.name("domain").value(domainName);
		writer.name("keywords");
			writer.beginArray();
			Iterator<String> itr = data.iterator();
			while (itr.hasNext())
				writer.value(itr.next());
			writer.endArray();
		writer.endObject();
		writer.close();
		
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
		    Collections.sort(termlist, new Lucene.ReverseComparator<String>(
		      new Lucene.ByValueComparator<String,Integer>(frequencyMap)));
		    
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
	
	
	/**************************************************************************
	 * 
	 ***************************************************************************/
	
	
	public static final String SMART_STOP_WORDS[] ={
		   "a",
		   "able",
		   "about",
		   "above",
		   "according",
		   "accordingly",
		   "across",
		   "actually",
		   "after",
		   "afterwards",
		   "again",
		   "against",
		   "all",
		   "allow",
		   "allows",
		   "almost",
		   "alone",
		   "along",
		   "already",
		   "also",
		   "although",
		   "always",
		   "am",
		   "among",
		   "amongst",
		   "an",
		   "and",
		   "another",
		   "any",
		   "anybody",
		   "anyhow",
		   "anyone",
		   "anything",
		   "anyway",
		   "anyways",
		   "anywhere",
		   "apart",
		   "appear",
		   "appreciate",
		   "appropriate",
		   "are",
		   "around",
		   "as",
		   "aside",
		   "ask",
		   "asking",
		   "associated",
		   "at",
		   "available",
		   "away",
		   "awfully",
		   "b",
		   "be",
		   "became",
		   "because",
		   "become",
		   "becomes",
		   "becoming",
		   "been",
		   "before",
		   "beforehand",
		   "behind",
		   "being",
		   "believe",
		   "below",
		   "beside",
		   "besides",
		   "best",
		   "better",
		   "between",
		   "beyond",
		   "both",
		   "brief",
		   "but",
		   "by",
		   "c",
		   "came",
		   "can",
		   "cannot",
		   "cant",
		   "cause",
		   "causes",
		   "certain",
		   "certainly",
		   "changes",
		   "clearly",
		   "co",
		   "com",
		   "come",
		   "comes",
		   "concerning",
		   "consequently",
		   "consider",
		   "considering",
		   "contain",
		   "containing",
		   "contains",
		   "corresponding",
		   "could",
		   "course",
		   "currently",
		   "d",
		   "definitely",
		   "described",
		   "despite",
		   "did",
		   "different",
		   "do",
		   "does",
		   "doing",
		   "done",
		   "down",
		   "downwards",
		   "during",
		   "e",
		   "each",
		   "edu",
		   "eg",
		   "eight",
		   "either",
		   "else",
		   "elsewhere",
		   "enough",
		   "entirely",
		   "especially",
		   "et",
		   "etc",
		   "even",
		   "ever",
		   "every",
		   "everybody",
		   "everyone",
		   "everything",
		   "everywhere",
		   "ex",
		   "exactly",
		   "example",
		   "except",
		   "f",
		   "far",
		   "few",
		   "fifth",
		   "first",
		   "five",
		   "followed",
		   "following",
		   "follows",
		   "for",
		   "former",
		   "formerly",
		   "forth",
		   "four",
		   "from",
		   "further",
		   "furthermore",
		   "g",
		   "get",
		   "gets",
		   "getting",
		   "given",
		   "gives",
		   "go",
		   "goes",
		   "going",
		   "gone",
		   "got",
		   "gotten",
		   "greetings",
		   "h",
		   "had",
		   "happens",
		   "hardly",
		   "has",
		   "have",
		   "having",
		   "he",
		   "hello",
		   "help",
		   "hence",
		   "her",
		   "here",
		   "hereafter",
		   "hereby",
		   "herein",
		   "hereupon",
		   "hers",
		   "herself",
		   "hi",
		   "him",
		   "himself",
		   "his",
		   "hither",
		   "hopefully",
		   "how",
		   "howbeit",
		   "however",
		   "i",
		   "ie",
		   "if",
		   "ignored",
		   "immediate",
		   "in",
		   "inasmuch",
		   "inc",
		   "indeed",
		   "indicate",
		   "indicated",
		   "indicates",
		   "inner",
		   "insofar",
		   "instead",
		   "into",
		   "inward",
		   "is",
		   "it",
		   "its",
		   "itself",
		   "j",
		   "just",
		   "k",
		   "keep",
		   "keeps",
		   "kept",
		   "know",
		   "knows",
		   "known",
		   "l",
		   "last",
		   "lately",
		   "later",
		   "latter",
		   "latterly",
		   "least",
		   "less",
		   "lest",
		   "let",
		   "like",
		   "liked",
		   "likely",
		   "little",
		   "look",
		   "looking",
		   "looks",
		   "ltd",
		   "m",
		   "mainly",
		   "many",
		   "may",
		   "maybe",
		   "me",
		   "mean",
		   "meanwhile",
		   "merely",
		   "might",
		   "more",
		   "moreover",
		   "most",
		   "mostly",
		   "much",
		   "must",
		   "my",
		   "myself",
		   "n",
		   "name",
		   "namely",
		   "nd",
		   "near",
		   "nearly",
		   "necessary",
		   "need",
		   "needs",
		   "neither",
		   "never",
		   "nevertheless",
		   "new",
		   "next",
		   "nine",
		   "no",
		   "nobody",
		   "non",
		   "none",
		   "noone",
		   "nor",
		   "normally",
		   "not",
		   "nothing",
		   "novel",
		   "now",
		   "nowhere",
		   "o",
		   "obviously",
		   "of",
		   "off",
		   "often",
		   "oh",
		   "ok",
		   "okay",
		   "old",
		   "on",
		   "once",
		   "one",
		   "ones",
		   "only",
		   "onto",
		   "or",
		   "other",
		   "others",
		   "otherwise",
		   "ought",
		   "our",
		   "ours",
		   "ourselves",
		   "out",
		   "outside",
		   "over",
		   "overall",
		   "own",
		   "p",
		   "particular",
		   "particularly",
		   "per",
		   "perhaps",
		   "placed",
		   "please",
		   "plus",
		   "possible",
		   "presumably",
		   "probably",
		   "provides",
		   "q",
		   "que",
		   "quite",
		   "qv",
		   "r",
		   "rather",
		   "rd",
		   "re",
		   "really",
		   "reasonably",
		   "regarding",
		   "regardless",
		   "regards",
		   "relatively",
		   "respectively",
		   "right",
		   "s",
		   "said",
		   "same",
		   "saw",
		   "say",
		   "saying",
		   "says",
		   "second",
		   "secondly",
		   "see",
		   "seeing",
		   "seem",
		   "seemed",
		   "seeming",
		   "seems",
		   "seen",
		   "self",
		   "selves",
		   "sensible",
		   "sent",
		   "serious",
		   "seriously",
		   "seven",
		   "several",
		   "shall",
		   "she",
		   "should",
		   "since",
		   "six",
		   "so",
		   "some",
		   "somebody",
		   "somehow",
		   "someone",
		   "something",
		   "sometime",
		   "sometimes",
		   "somewhat",
		   "somewhere",
		   "soon",
		   "sorry",
		   "specified",
		   "specify",
		   "specifying",
		   "still",
		   "sub",
		   "such",
		   "sup",
		   "sure",
		   "t",
		   "take",
		   "taken",
		   "tell",
		   "tends",
		   "th",
		   "than",
		   "thank",
		   "thanks",
		   "thanx",
		   "that",
		   "thats",
		   "the",
		   "their",
		   "theirs",
		   "them",
		   "themselves",
		   "then",
		   "thence",
		   "there",
		   "thereafter",
		   "thereby",
		   "therefore",
		   "therein",
		   "theres",
		   "thereupon",
		   "these",
		   "they",
		   "time",
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
		   "under",
		   "unfortunately",
		   "unless",
		   "unlikely",
		   "until",
		   "unto",
		   "up",
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
		   "whenever",
		   "where",
		   "whereafter",
		   "whereas",
		   "whereby",
		   "wherein",
		   "whereupon",
		   "wherever",
		   "whether",
		   "which",
		   "while",
		   "whither",
		   "who",
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
		   "would",
		   "x",
		   "y",
		   "yes",
		   "yet",
		   "you",
		   "your",
		   "yours",
		   "yourself",
		   "yourselves",
		   "z",
		   "zero", 
		   "house",
		   "reach",
		   "tonight",
		   "today",
		   "wei",
		   "watch",
		   "play",
		   "home",
		   "meet",
		   "email",
		   "test",
		   "win",
		   "0",
		   "1",
		   "2",
		   "3",
		   "4",
		   "5",
		   "6",
		   "7",
		   "8",
		   "9",
		   "i'm",
		   "don't",
		   "http",
		   "gmail",
		   "sms",
		   "pm"
		   };
	
	
	public static class ByValueComparator<K,V extends Comparable<? super V>> implements Comparator<K> {

		private Map<K,V> map = new HashMap<K,V>();

		public ByValueComparator(Map<K,V> map) {
			this.map = map;
		}

		public int compare(K k1, K k2) {
			return map.get(k1).compareTo(map.get(k2));
		}

	}
	
	public static class ReverseComparator <T> implements Comparator<T>, Serializable {

	    /*
	     * Serialization version from Collections 2.0.
	     */
	    private static final long serialVersionUID = 2858887242028539265L;

	    /**
	     * The comparator being decorated.
	     */
	    private Comparator<T> comparator;

	    /**
	     * Creates a comparator that inverts the comparison
	     * of the given comparator.  Pass in a {@link ComparableComparator}
	     * for reversing the natural order, as per
	     * {@link java.util.Collections#reverseOrder()}</b>.
	     *
	     * @param comparator Comparator to reverse
	     */
	    public ReverseComparator(Comparator<T> comparator) {
	        this.comparator = comparator;
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Compares two objects in reverse order.
	     *
	     * @param obj1 the first object to compare
	     * @param obj2 the second object to compare
	     * @return negative if obj1 is less, positive if greater, zero if equal
	     */
	    public int compare(T obj1, T obj2) {
	        return comparator.compare(obj2, obj1);
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Implement a hash code for this comparator that is consistent with
	     * {@link #equals(Object) equals}.
	     *
	     * @return a suitable hash code
	     * @since Commons Collections 3.0
	     */
	    public int hashCode() {
	        return "ReverseComparator".hashCode() ^ comparator.hashCode();
	    }

	    /**
	     * Returns <code>true</code> iff <i>that</i> Object is
	     * is a {@link Comparator} whose ordering is known to be
	     * equivalent to mine.
	     * <p/>
	     * This implementation returns <code>true</code>
	     * iff <code><i>object</i>.{@link Object#getClass() getClass()}</code>
	     * equals <code>this.getClass()</code>, and the underlying
	     * comparators are equal.
	     * Subclasses may want to override this behavior to remain consistent
	     * with the {@link Comparator#equals(Object) equals} contract.
	     *
	     * @param object the object to compare to
	     * @return true if equal
	     * @since Commons Collections 3.0
	     */
	    public boolean equals(Object object) {
	        if (this == object) {
	            return true;
	        } else if (null == object) {
	            return false;
	        } else if (object.getClass().equals(this.getClass())) {
	            @SuppressWarnings("rawtypes")
				ReverseComparator thatrc = (ReverseComparator) object;
	            return comparator.equals(thatrc.comparator);
	        } else {
	            return false;
	        }
	    }

	}
	
	
	static class Domain {
		String domain;
		String[] keywords;
		
		public Domain(String d, String[] k){
			this.domain = d;
			this.keywords = k;
		}
	}
}


