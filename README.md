News-Butler
===========

CS4274 Project
===========

\server contain 3 files
  \articles -- web_article scraped by Lucene 
  \article_index -- indices created by Lucene
  \ranking -- stores top terms in json format
  - Lucene.java - Articles indexer
  - scraper.py - scraps sports articles and invokes Lucene.java
  - server.py - server hosting domain terms
  
To compile Lucene:

Windows
javac -classpath .;* Lucene.java
java -cp .;*; Lucene 

Unix
javac -classpath .:* Lucene.java
java -cp .:*: Lucene 

client
  - remember to check server IP
