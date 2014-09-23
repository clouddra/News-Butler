News-Butler
===========
A proof-of-concept Android news app which learns the preferences of user.


\server contain 3 files
  \articles -- web_article scraped by Lucene 
  \article_index -- indices created by Lucene
  \ranking -- stores top terms in json format
  - Lucene.java - Articles indexer
  - scraper.py - scraps sports articles and invokes Lucene.java
  - server.py - server hosting domain terms
  
To compile Lucene:

Windows:
javac -classpath .;* Lucene.java
java -cp .;*; Lucene 

Unix:
javac -classpath .:* Lucene.java
java -cp .:*: Lucene 



Android App
  - remember to check server IP
