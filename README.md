# WikiI5Converter

The [Institut für Deutsche Sprache (IDS)](http://www1.ids-mannheim.de/) develops a corpus builder for Wikipedia. The purpose of the tool is to convert Wikipedia pages from its native text format, wikitext, into our target corpus format, I5.

I5 is the IDS text model used in [Das Deutsche Referenzkorpus (DeReKo)](http://www1.ids-mannheim.de/kl/projekte/korpora/). It is a customized TEI format based on XCES, enriched with metadata information on different corpus structure levels (Lüngen and Sperberg-McQueen, 2012). 

As part of DeReKo, Wikipedia corpora built using this tool, are accessible through [Corpus Search, Management and Analysis System II (COSMAS II)](http://www.ids-mannheim.de/cosmas2/) and [Corpus Analysis Platform (KorAP)](https://korap.ids-mannheim.de/kalamar).

The corpus builder operates in two stage conversion (Margaretha and Lüngen, 2014). In the first stage, WikiXMLConverter converts wikitext into WikiXML by using [Sweble Parser](http://sweble.org/) and generates a WikiXML file for each wikipage within a wikipedia namespace, for instance articles. In the second stage, [WikiI5Converter](https://github.com/IDS-Mannheim/WikiI5Converter) converts each WikiXML file into I5 using XSLT Stylesheets and assemble them altogether as a single corpus file as required for DeReKo.

In addition to building article corpus, the corpus builder is also designed for building Computer Mediated Communication (CMC) corpora from Wikipedia talk or discussion pages, such as in the Talk and User talk [namespaces](https://en.wikipedia.org/wiki/Wikipedia:Namespace). The talk corpus is structured by means of postings and threads from users following the TEI scheme for CMC corpus (Beißwenger, et al., 2012). Our posting segmentation is done heuristically in WikiXMLConverter.

The corpus builder is able to parse Wikipedia of multiple languages. It has been tested for the following languages: english, french, hungarian, norwegian, spanish, croatian, italian, polish and rumanian. We provides Wikipedia corpora of these languages in WikiXML and I5 formats for [download](http://www1.ids-mannheim.de/direktion/kl/projekte/korpora/verfuegbarkeit.html).


## Instructions

The XSLT transformation in WikiI5Converter is done by using a Saxon-EE library, which is a the commercial variant of Saxon. In the development, we use saxon9ee-9.4.0.3J.jar. Both the saxon-ee jar and its license (saxon-license.lic) are needed for the conversion and should be put together in a lib/ folder. While running the WikiI5Converter tool, the lib/ folder must be added to the java classpath. The following describes the command format to run the WikiI5Converter tool in a terminal.

```java -Xmx4g -cp "[jar-file]:lib/*:." [main-class] -prop [properties-file-path] > [log-file-path] 2>&1```

The main class of the WikiI5Converter is ```de.mannheim.ids.wiki.WikiI5Converter```. Similar to the [WikiXMLConverter](https://github.com/IDS-Mannheim/WikiXMLConverter), it is advisable to set the java memory allocation pool to convert a full wikipedia and log the console output.

Command example:

```java -Xmx4g -cp "code/WikiI5Converter-1.0.1-jar-with-dependencies.jar:lib/*:." de.mannheim.ids.wiki.WikiI5Converter -prop properties/i5-dewiki-article.properties > logs/wikiI5-dewiki-20150808-article.log 2>&1```

## Properties

WikiI5Converter requires the following properties in a properties file:

* ```wikidump = dewiki-20130728-pages-meta-current.xml```

    The name of the wikidump is used to create the final wiki I5 corpus metadata.

* ```page_type = article```

  The type of the Wikipedia pages.

* ```language = Deutsch```

    The language name of the wikipedia dump in its language.

* ```korpusSigle = WPD13```

    The code or identifier for the corpus level.

* ```namespace_key = 0```

    The namespace of the WikiXML files.

* ```max_threads = 2```

    The number of maximum threads allowed to run concurrently

* ```WikiXML_folder = wikixml-de/articles```

    The location of WikiXML files to convert (root folder).

* ```WikiXML_index = index/dewiki-article-index.xml```
    
    The index of the WikiXML files to convert. See [WikiXMLIndex](https://github.com/IDS-Mannheim/WikiI5Converter/new/master?readme=1#wikixml-index) to generate such index.
    
* ```output_file = i5/de/dewiki-20130728-articles.i5.xml```

    The output filename/path, namely where and how the final corpus should be named.

* ```output_encoding = iso-8859-1```

    The encoding of the output file (how the final corpus should be encoded). See [Encoding](https://github.com/IDS-Mannheim/WikiI5Converter/new/master?readme=1#encoding).

* ```inflective_file = inflectives.xml```

    The location of the inflectives file. See [Inflectives](https://github.com/IDS-Mannheim/WikiI5Converter/new/master?readme=1#inflectives).
    
* ```db_url = jdbc:mysql://localhost:port/dbname```

    The URL of the database containing the corresponding ```langlinks``` table. See [Language Links](https://github.com/IDS-Mannheim/WikiI5Converter/new/master?readme=1#language-links).
    
* ```db_username = username```

    The username to connect to the database.
    
* ```db_password = password```

    The corresponding password to the username to connect to the database.

*```creator = creator name```

    The name of the person running the builder.
 

## WikiXML index

The WikiXML index lists all the WikiXML files generated by [WikiXMLConverter](https://github.com/IDS-Mannheim/WikiXMLConverter). This index can be generated by using the shell script ```WikiXMLCorpusIndexer.sh``` that takes 3 arguments: 

        ```[Wiki page type] [WikiXML corpus folder] [output file]```


The Wiki page type will be the root element of the index file and therefore must be in plural form, for instance articles, talks and user-talks.
    
 Command example:
 
 ```./WikiXMLCorpusIndexer.sh articles WikiXML-de/article article-index.xml```


will create an index (article-index.xml) of all the WikiXML files in the folder WikiXML-de/article. The index will have the following structure:

<pre>

    &lt;articles&gt;
     &lt;index value="0"&gt;
        &lt;id>179&lt;/id&gt;
         ...
     &lt;/index&gt;
     ...
     &lt;index value="A"&gt;
        &lt;id>1&lt;/id&gt;
          ...
     &lt;/index&gt;
          ...          
    &lt;/articles&gt;

</pre>

A WikiXML index is required by the WikiI5Converter to access wiki pages and arrange them in the final I5 corpus. Thus, it is necessary to index the WikiXML files before running the WikiI5Converter.

## Language links

Most of wikipedia articles have analogs in the other wikipedias of other languages. The links to these analog pages are not explicitly written in the wikidumps, but stored separately in a form of a database table. A wikipedia langlinks table contains information of all the page titles in different languages. The analog page titles of a wiki page are connected to each other by means of its page id.

To obtain this language link information, firstly download the corresponding language link sql dump of the wikipedia dump and restore it to a database. 

Create a MySQL database using the following command in a MySQL shell:

```CREATE DATABASE [database_name];```

In a unix shell, restore the language link dump:

```mysql -u root -p [database_name] < [dump_file]```

The database will contain a table ```langlinks``` with 3 columns: 
* ```ll_from``` lists the wiki page ids
* ```ll_lang``` lists the language of the wikipedias
* ```ll_title``` lists the page title in different languages.

Rename the table name ```langlinks``` to ```[2-letter languagecode]_langlinks``` to distinguish the tables of wikipedias of different languages. For instance, ```langlinks``` table from a german wikipedia dump should be renamed into ```de_langlinks``` by using the following MySQL command:

```RENAME TABLE langlinks TO de_langlinks;```

Using the langlinks table, wikipedia page titles across the wikipedias of different languages can be listed by wiki page ids. For instance, the following command:

```SELECT * FROM de_langlinks WHERE ll_from = 3;```

lists all the wikipedia page titles in other languages for the german wikipage of id number 3. A snapshot of the results is shown below.

<pre>
+---------+----------+--------------------------------------------+
| ll_from | ll_lang  | ll_title                                   |
+---------+----------+--------------------------------------------+
|       3 | af       | Aktinium                                   | 
|       3 | am       | አክቲኒየም                                     | 
|       3 | ar       | أكتينيوم                                      | 
|       3 | ast      | Actiniu                                    | 
|       3 | az       | Aktinium                                   | 
|       3 | be       | Актыній                                    | 
</pre>
From this table, language links to other wikipedias can be easily generated by using the following format:

```https://[ll_lang].wikipedia.org/wiki/[ll_title]```

Besides, this information is needed to create the ```<relatedItem type="langlink">``` in each wiki page (```<idsText>```) in a wiki I5 corpus. Thus, before running WikiI5Converter, it is necessary to first restore and rename the langlinks table that corresponds to the current wikidump.

## Corpus structure
              
An I5 corpus has a tripartite structure, consisting of the corpus, document and text level. The root element is ```<idsCorpus>``` having ```korpusSigle``` as identifier, for instance WPD15 denotes German wiki I5 corpus from a dump taken in 2015. The ```<idsCorpus>``` element consist of many ```<idsDoc>``` elements, and an ```<idsDoc>``` contain many ```<idsText>``` elements. Each of these elements has an ```<idsHeader>``` describing the contents of the correpondding elements.

Similar to the WikiXML index, each ```<idsDoc>``` is ordered by alphabets and numbers which represents the first character of the page/text titles. The documents are further grouped by the text ids, that is every 10000 ids. Document identifiers known as ```documentSigle``` are 1-letter and 2-digit text group ids combined with the ```korpusSigle```. For instance, ```WPD15/A00``` contains all texts with ids are between 1 to 99999, and ```WPD15/A01``` between 100000 to 199999. Text identifiers combine the ```korpusSigle``` and their corresponding ```dokumentSigle``` with the text id. For instance, ```WPD15.A00.00001``` is the idsText's textSigle i.e. the wikipage with id 1 having a title starting with A.

In some cases, for instance the English wikipedia, the length of the wiki page ids exceeds 7 digits. For these wiki pages, exceptional textSigles are generated with length 10 including the 2 digits in the text groups. For instance, ```WPD15.A00.12345678``` is generated for the wiki page with id ```12345678```.  

## XSLT Transformation

The XSLT transformation is only done for ```<idsText>```. The other corpus structures and document sorting are done in Java. The main XSLT file creates the ```<idsHeader>``` for ```<idsText>``` and groups WikiXML by headings (see ```/src/main/resources/Templates.xsl```). It includes the additional XSLT file containing various templates handling different XML tags.

## Logs

The log file lists all the WikiXML files going through the transformation process. Not all WikiXML files, however, are successfully transformed. The statistics of the transformation process is summarized in the end of the log file. The summary of the german wiki I5 conversion is shown below.

<pre>
Number of transformed pages: 1802682
Number of not-transformed pages (char index): 6939
Number of transformation errors: 0
Number of empty transformation results: 0
Number of DTD validation errors: 9
Number of non well-formed XML: 0
Total number valid pages: 1802673

WikiI5Converter execution time 12:47:32
</pre>

As described in the WikiXMLIndex section, some WikiXML files whose titles cannot be normalized are put into a char/ folder, and these files are ignored for transformation. Transformation errors are errors that are thrown by Saxon API during the XSLT transformation. After a successful transformation, the resulting ```<idsText>``` elements are validated against the [I5 DTD](http://corpora.ids-mannheim.de/I5/DTD/i5.dtd) by using a SAX Parser. The number of DTD validation errors shows the total of invalid pages. The total number of valid pages shows the number of wiki pages in the final corpus.

Note: empty transformation results and non-wellformed XML are not used anymore and should be removed in the next version.


## Inflectives

Interaction words or inflectives orthographically represent speaker’s actions, gestures or facial expressions (e.g. ```*giggle*```) in a written conversation. They appear frequently in the conversations among the users in wikipedia talk pages and are of particular interest in the linguistic analysis of CMC. Sometimes, interaction words are even used as (pseudo) markup, as in ```<seufz>nicht</seufz>```. 

I5 adopted the CMC structured proposed by Beißwenger et al. (2012) as an extension to the TEI P5 scheme. In I5, pseudo markup with interaction words is encoded as in the following:

<pre>
  &lt;p>Bitte 
   &lt;interactionTerm&gt;
      &lt;interactionWord id="WDD15.A00.00131-1-iaw1" n="1" 
      next="WDD15.A00.00131-2-iaw2" topology="openingTag"&gt;
      &amp;lt;seufz&amp;gt;&lt;/interactionWord&gt;
   &lt;/interactionTerm&gt;
  nicht
   &lt;interactionTerm&gt;
      &lt;interactionWord id="WDD15.A00.00131-2-iaw2" n="2" 
      prev="WDD15.A00.00131-1-iaw1" topology="closingTag"&gt;
      &amp;lt;/seufz&amp;gt;&lt;/interactionWord&gt;
   &lt;/interactionTerm&gt;
  alles mit Grossbuchstaben beginnen, sondern und neue 
  Rechtschreibung, wenn möglich&lt;/p&gt;

</pre>

WikiI5Converter relies on a list of interaction words to recognize them in WikiXML and it only deals with interaction words that are marked as tags, for instance ```<seufz>```. The lists of interaction words vary for different languages. We only provide a list of interactionWord for German. The interaction words (inflectives) are contained on the file that is givern as “inflective_list” in the properties file and must be listed as follows.

<pre>
&lt;inflectives&gt;
  &lt;name>abgreif&lt;/name&gt;
  &lt;name>auf-die-Nägel-blas&lt;/name&gt;
  &lt;name>Augenverdreh&lt;/name&gt;
  &lt;name>ausrück&lt;/name&gt;
  &lt;name>dazwischen-quetsch&lt;/name&gt;
  &lt;name>dazwischenquetsch&lt;/name&gt;
  &lt;name>Dazwischenquetsch&lt;/name&gt;
  &lt;name>Doppelseufz&lt;/name&gt;
         …
&lt;/inflectives&gt;
</pre>


## Footnotes

In wikitext, footnote descriptions are typically written directly where the footnotes themselves are placed. In the instance below, a footnote is marked by the element ```<ref>```.

<pre>
Von 1968 bis 2000 wurde es von der [[Directors Guild of America]] (DGA) für solche Situationen empfohlen, seither ist es '''Thomas Lee'''.&lt;ref&gt;[[Los Angeles Times|latimes]].com: [http://articles.latimes.com/2000/jan/15/entertainment/ca-54271 ''Name of Director Smithee Isn't What It Used to Be''], zuletzt geprüft am 2. April 2011&lt;/ref&gt; 
</pre>

In I5, we make a distinction between footnote places and descriptions. In a footnote place, a ```<ptr>``` element is added refering to a ```<note>``` element in the footnote list. The footnote list includes the desciptions of all footnotes. It is written in the ```<back>``` element after all text is written in the ```<body>``` element. 

A pointer at the footnote appearance:

<pre>
Von 1968 bis 2000 wurde es von der &lt;ref target="https://de.wikipedia.org/wiki/Directors_Guild_of_America" targOrder="u"&gt;Directors Guild of America&lt;/ref&gt; (DGA) für solche Situationen empfohlen, seither ist es &lt;hi rend="bo" &gt;Thomas Lee&lt;/hi&gt;.&lt;ptr target="WPD17.A00.00001-f1" rend="ref" targType="note" targOrder="u"/&gt;
</pre>

A footnote list:
<pre>
&lt;back&gt;
    &lt;div n="1" complete="y" type="footnotes" part="N" org="uniform" sample="complete"&gt;
        &lt;note id="WPD17.A00.00001-f1" place="foot" anchored="true"&gt;
            &lt;ref target="https://de.wikipedia.org/wiki/Los_Angeles_Times" targOrder="u"
                &gt;latimes&lt;/ref&gt;.com:&lt;ref
                target="http://articles.latimes.com/2000/jan/15/entertainment/ca-54271"
                targOrder="u"/&gt;, zuletzt geprüft am 2. April 2011&lt;/note&gt;
        &lt;note id="WPD17.A00.00001-f2" place="foot" anchored="true"&gt;
            &lt;ref target="http://www.imdb.com/name/nm0000647/" targOrder="u"&gt;Eigener Eintrag für in
                der IMDb&lt;/ref&gt;
        &lt;/note&gt;        
    &lt;/div&gt;
&lt;/back&gt;
</pre>  


## Categories

Wikipedia articles often contain category links at the end of their pages. In I5, the category links are placed in the ```<idsHeader>``` element of the corresponding ```<idsText>``` element of a Wikipedia page.

<pre>
&lt;idsText id="WPD17.A00.00001" n="de.Alan_Smithee" version="1"&gt;
    &lt;idsHeader type="text" pattern="text" status="new" version="1.0" TEIform="teiHeader"&gt;
        &lt;profileDesc&gt;
            &lt;textClass Default="n"&gt;
                &lt;classCode scheme="https://en.wikipedia.org/wiki/Portal:Contents/Categories"&gt;
                    &lt;ref target="https://de.wikipedia.org/wiki/Kategorie%3AFiktive_Person"
                        targOrder="u"&gt;Smithee, Alan&lt;/ref&gt;
                    &lt;ref target="https://de.wikipedia.org/wiki/Kategorie%3APseudonym" targOrder="u"
                        &gt;Kategorie:Pseudonym&lt;/ref&gt;
                    &lt;ref target="https://de.wikipedia.org/wiki/Kategorie%3ASammelpseudonym"
                        targOrder="u"&gt;Smithee, Alan&lt;/ref&gt;
                    &lt;ref target="https://de.wikipedia.org/wiki/Kategorie%3AWerk_von_Alan_Smithee"
                        targOrder="u"&gt;Kategorie:Werk von Alan Smithee&lt;/ref&gt;
                &lt;/classCode&gt;
            &lt;/textClass&gt;
        &lt;/profileDesc&gt;
    &lt;/idsHeader&gt;
&lt;/idsText&gt;

</pre>


## References
              
Beißwenger, M., Ermakova, M., Geyken, A., Lemnitzer, L., and Storrer, A. (2012). A tei schema for the representation of computer-mediated communication. Journal of the Text Encoding Initiative [Online], 3.

Margaretha, E., and Lüngen,H. (2014). Building linguistic corpora from Wikipedia articles and discussions.  Journal for Language Technologie and Computational Linguistics (JLCL), 2/2014.

Lüngen, H., and Sperberg-McQueen, C. M. (2012). A TEI P5 Document Grammar for the IDS Text Model. Journal of the Text Encoding Initiative [Online], 3. URL : http://jtei.revues.org/508 ; DOI : 10.4000/jtei.508

