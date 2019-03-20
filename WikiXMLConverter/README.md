
# WikiXMLConverter

WikiXMLConverter builds Wikipedia corpora in WikiXML format. It converts each Wikipedia page written in Wikitext into a WikiXML page. Therefore, a WikiXML corpus consists of many WikiXML pages, unlike a single-file I5 corpus built by WikiI5Converter.

## Instructions
To run WikiXMLConverter, a Wikipedia dump and a properties file is required. Wikipedia dumps can be downloaded from ```https://dumps.wikimedia.org/```. A complete Wikipedia dump including article and talk pages typically has the following format: 
<pre>
  [languagecode]wiki-[latest or date]-pages-meta-current.xml
</pre> 

To convert a full Wikipedia (not only a small sample), we advise to increase and limit the java memory allocation pool by using -Xmx parameter. For instance, -Xmx4g to set the maximum Java heap size to 4 Gigabytes. The example below shows a command to run WikiXMLConverter in a terminal. The outputs and error messages are to be written into the a log file.

<pre>
java -jar -Xmx4g [jar-file-path] -prop [properties-file-path] > [log-file-path] 2>&1
</pre>

The main class of this project is ```/src/main/java/de/mannheim/ids/wiki/WikiXMLConverter.java```, that is the starting point of the conversion process. Besides, ```/src/main/java/de/ mannheim/ids/wiki/WikiXMLProcessor.java``` is the class managing the overall conversion process.

## Properties

### Article properties
For article pages, the properties files requires the following properties:

* ```wikidump = data/dewiki-20130728-sample.xml```

  The Wikipedia dump file path.

* ```page_type = article```

  The type of the Wikipedia pages.

* ```language_code = de```

  Two letter language code of the Wikipedia.
    
* ```output_encoding = utf-8```
  
  The encoding of the WikiXML output files.
  
* ```namespace_key = 0```
  
  The namespace key of the Wikipedia pages to convert, for instance  the namespace key for articles is 0, talk pages is 1, and user talk pages is 3. See [Wikipedia:Namespace](https://en.wikipedia.org/wiki/Wikipedia:Namespace). 
  
* ```max_threads = 4```
  
  The maximum number of threads allowed to run concurrently. It should be the number of CPUs -1.
  
* ```generate_wikipage = true```
  
  The option to generate additional Wikipage files in Wikitext: true or false (default). These files are not part of the resulting WikiXML corpus and can be used for instance to examine specific Wikipages without having to extract them from the large Wikipedia dump.

* ```exclude_page_id = 123,456```
  
  A list of ids of pages to be excluded from the conversion, separated by comma. This option allows the converter to exclude problematic Wikipedia pages, for instance because of broken mark-up usages causing very slow or infinite conversion. 


### Talk properties

For talk pages, additional properties are required as follows:
* ```user_page = Benutzer```

User page prefix in the Wikidump language, for instance ```User``` in English, ```Benutzer``` in German. See [Wikipedia:User pages](https://en.wikipedia.org/wiki/Wikipedia:User_pages). 

* ```user_contribution = Spezial:Beiträge```

The prefix of user contribution pages in Wikitext, for instance ```Special:Contributions``` in English, ```Spezial:Beiträge``` in German. See [Help:User contributions](https://en.wikipedia.org/wiki/Help:User_contributions).

* ```signature = Hilfe:Signatur```

Signature page in the Wikidump language, for instance ```Wikipedia:Signatures``` in English, ```Hilfe:Signatur``` in German. See: [Wikipedia:Signatures](https://en.wikipedia.org/wiki/Wikipedia:Signatures).

* ```unsigned = unsigniert```

Unsigned template in the Wikidump language, for instance ```unsigned``` in English, ```unsigniert``` in German, ```non signé``` in French. See [Template:Unsigned](https://en.wikipedia.org/wiki/Template:Unsigned). 


* ```title_prefix = Wikipedia:Löschkandidaten```

  The prefix of Wikipedia page titles to convert (optional). This option allows further filtering of pages belongs to a Wikipedia namespace. For instance, ```Wikipedia:Löschkandidaten``` pages are part of the ```Wikipedia``` namespace containing also other pages having other titles such as ```Wikipedia:Aktuelles```.  


## Outputs

The output of this conversion is a WikiXML corpus, a collection of WikiXML files organized alphabetically and numerically by the page titles in separate folders. The WikiXML files are named based on the Wikipage ids. Sometimes, a Wikipage title starts with a character that cannot be normalized into an alphanumerical character. These pages are stored in Char/ folder and are ignored in the second stage conversion. The tree structure below illustrates how the WikiXML output files are organized.

<pre>
WikiXML-de/
|_______ article/
        |_______ A/
        |       |_______ 1.xml
        |       |_______ 2.xml
        |       |_______ 3.xml
…..
        |_______ B/
        |_______ C/
        |           …..
        |_______ 0/
        |        …..
        |_______ 9/
        |_______ Char/
|_______ talk/
        |_______ A/
        |        …..
        |_______ Char/

</pre>

## Logs

The log file lists all the WikiXML files (and Wikitext files) generated through the conversion. At the end of the file, the number of files and the exceptions/errors encountered during the conversion are summarized. Below is an example of a summary from the conversion of the articles of the German Wikipedia.

<pre>
Total pages (without redirect pages) 1810405
Total non-empty pages 1809630
Total redirect pages 1256662
Total empty pages 0
Total empty parsed pages 765
Total pages without id 0
Total Sweble exceptions 0
Total Renderer exceptions 765
Total DOM exceptions 10
Total XML Page structure exceptions 0
Total thread deaths: 0
Total unknown errors: 0

WikiXMLConverter execution time 1:34:49
</pre>

Empty parsed pages shows the number of pages that become empty after the parsing process, which can be caused by Sweble exceptions or Renderer exceptions. Sweble exceptions are exceptions that are thrown by the Sweble parser and cause the parsing of the corresponding Wikipage to break off. After a successful parsing resulting in an abstract syntax tree, a renderer exception might be thrown within the rendering process, namely while generating WikiXML from the abstract syntax tree.

The generated WikiXML data is not guaranteed to be well-formed. Thus, an XML well-formedness check is performed using a DOM Parser. If a generated WikiXML is not well-formed, a DOM exception will be thrown. Apart from the Wikitext, which is the text content a Wikipage, metadata of a Wikipage is also incorporated in a WikiXML file. The page metadata structure is also checked for well-formedness.

## Postings

Wikipedia talk pages contain discussions among users about the corresponding Wikipedia articles. In a typical computer-mediated communication corpus, a posting is described as a contribution to a written dialogue, similar to an utterance in a spoken conversation. It is a piece of text sent/posted/submitted to a server at one point of time. A collection of sequential postings within the same subject forms a conversation thread.

In Wikipedia, however, postings are not necessarily sequential because any users may write their contributions anywhere in the texts of Wiki talk pages. Moreover, they may remove or edit parts of existing texts. Thus, the boundaries between postings are sometimes unclear. WikiXMLConverter makes use of a heuristic approach to segment Wikitext and at the same time create postings from the segments (Margaretha and Lüngen, 2014).

See ```/src/main/java/de/mannheim/ids/wiki/page/WikiPostHandler.java```
 
## Signatures

Most postings are explicitly signed, unsigned or marked by other users. Three types of signatures are defined:

* ```Signed signatures```

are signatures that are explicitly signed by registered authors by using tildes. See [Wikipedia:Signature](https://en.wikipedia.org/wiki/Wikipedia:Signature).

* ```Unsigned signatures```

are signatures that are added by registered or unregistered users to mark an existing unsigned posting. See [Template:Unsigned](https://en.wikipedia.org/wiki/Template:Unsigned).
    
* ```User contributions```

are signatures that are added by unregistered users. For instance, when an unregistered user uses four tildes to sign his post, a special contribution link based on his ip-address will be generated instead of a user link for a registered user. In general, user contributions signify all changes made by users. See [Help:User contributions](https://en.wikipedia.org/wiki/Help:User_contributions).


The signature types can be specified in <autoSignature> elements. All the signature strings are automatic signatures because users write only signature codes in Wiki mark-up, while the actual signature strings are automatically generated from the mark-up by the Wiki software.

See ```/src/main/java/de/mannheim/ids/wiki/page/WikiPostHandler.java```

## Timestamps

Posting signatures usually contain information about the posting date, time and time zone. These timestamp information is useful for linguists to discover language trends, for instance to find out when a new word has appeared and started to be used widely. The time information stored in attributes or in a timeline in the metadata section cannot be used by Cosmas II. Therefore, timestamps are kept in the running text of the corpus. Moreover, an external XML list of timestamps is also generated.

Wiki mark-up for timestamps differs in Wikipedias of different languages, although some languages have similar timestamp formats. In WikiXMLConverter, different regex patterns were created to handle different timestamp formats. The patterns allows minor differences of the formats enabling them to handle some variations in the timestamps.

<pre>
German timestamp format
11:38, 23. Jan. 2008 (CET)

Norwegian timestamp format
11. feb 2008 kl. 02:27 (CET). 

French timestamp format
10 décembre 2007 à 12:02 (CET)

Hungarian timestamp format
2006. október 17., 00:30 (CEST)
</pre>

See ```/src/main/java/de/mannheim/ids/wiki/page/WikiTimestamp.java```

## References

Beißwenger, M., Ermakova, M., Geyken, A., Lemnitzer, L., and Storrer, A. (2012). A tei schema for the representation of computer-mediated communication. Journal of the Text Encoding Initiative [Online], 3.

Margaretha, E., and Lüngen,H. (2014). Building linguistic corpora from Wikipedia articles and discussions.  Journal for Language Technologie and Computational Linguistics (JLCL), 2/2014.

Lüngen, H., and Sperberg-McQueen, C. M. (2012). A TEI P5 Document Grammar for the IDS Text Model. Journal of the Text Encoding Initiative [Online], 3. URL : http://jtei.revues.org/508 ; DOI : 10.4000/jtei.508
