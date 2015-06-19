#!/bin/bash
# ------------------------------------------------------------------------------------------
# WIKIPEDIA CONVERSION
#
# This script runs a set of programs that converts a wikipedia dump containing wiki mark-ups 
# (wikitext) into a wikipedia corpus in I5 format.
#
# The conversion is done in two stages. In the first stage, each wikipage is converted into 
# an XML file (WikiXML) by using WikiXMLConverter-0.0.1-jar-with-dependencies.jar. In the 
# second stage, each WikiXML page is converted into I5 and collected into a single corpus file
# by using WikiI5Converter-0.0.1.jar. The jars are available on http://corpora.ids-mannheim.de
# /pub/tools/.
#
# The I5 corpus is subsequently validated against IDS I5 DTD (see http://corpora.ids-mannheim.
# de/I5/DTD/i5.dtd) by using XMLlint. It is also validated against SGML by ONSGML (e.g. using 
# OpenSP-1.5.1). 
#
# This script takes input arguments sequentially as follows:
#  1. The type of wikipedia pages [articles|discussions]. 
#  2. 2-letter language code of the wikitext (e.g. de). 
#  3. The location of the wikidumps.
#
#     The filename of wikipedia dumps must be in the following format: 
#       [lang]wiki-[date]-pages-meta-current.xml
#
#  4. The desired encoding output, e.g iso-8859-1 or utf-8 (default).
#  5. The list inflectives (currently provided only for german).
#
# The outputs of the first stage conversion are grouped by letters and digits, and located on: 
#  - xml/articles for article pages
#  - xml/discussions for discussion pages
#
# The second stage convertion requires lists of all the article and discussion pages. The 
# function createList does this job. The article pages are listed in xml/articleList.xml and  
# the discussion pages in xml/discussionList.xml.
#
# Logs of the conversion process are located in logs/ folder.
#
#
# Eliza Margaretha, Mar 2013
# Institut für Deutsche Sprache
# --------------------------------------------------------------------------------------------

function createList {
    echo "<"$1">" > $2
    
    for index in $(find $3/$1/ -type d | sed 's/.*\///' | sort);
    do  
        echo "  <index value=\""$index"\">" >> $2 
        find $3/$1/$index/ -type f 2>/dev/null | sed 's/.*\/\(.*\)\.xml/\1/' | sort -n | sed 's/\(.*\)/    <id>\1<\/id>/' >> $2        
        echo "  </index>" >> $2
    done

    echo "</"$1">" >> $2    
}

# Arguments

pageType=$1
lang=$2
wiki=$3
encoding=$4
inflec=$5

if [ -z "$1" ];
then
    echo "Please specify the Wikipage type [articles|discussions]:  "
    read pageType
fi

if [ -z "$2" ];
then
    echo "Please specify the wikitext language (e.g. de) : "
    read lang
fi

if [ -z "$3" ];
then
    echo "Please specify the wikipedia dump location : "
    read wiki
fi

if [ -z "$4" ];
then     
     encoding=UTF-8
fi

if [ ! -d "logs" ];
then
    mkdir logs
fi 

# Variables

w=wiki
filename=$(basename "$wiki")
prefix=${filename%-pages*}
xmlFolder=xml-$lang 
wikiToXMLlog=xml-$prefix-$pageType.log
dtd=dtd/i5.dtd

if [ "$pageType" == "articles" ] 
then
    
    echo "Converting $lang-wiki articles to XML ..." 
    java -jar target/WikiXMLConverter-0.0.1-jar-with-dependencies.jar -l $lang -w $wiki -t articles -o $xmlFolder -e $encoding > logs/$wikiToXMLlog 2>&1 

    articleList=$xmlFolder/articleList.xml
    articleOutput=i5/$prefix-articles.i5
    articleLog=i5-$prefix-articles.log 
    
    echo "Listing $lang-wiki articles by index ..."    
    createList articles $articleList $xmlFolder

    echo "Converting $lang WikiXML articles to I5 ..."  

    if [ ! -d "i5" ];
    then
       mkdir i5
    fi

    if [ -z "$inflec" ]
    then
        java -Xmx10g -jar target/WikiI5Converter-0.0.1.jar -x $xmlFolder/articles -t articles -i $articleList -w $filename -o $articleOutput -e $encoding > logs/$articleLog 2>&1
    else
        java -Xmx10g -jar target/WikiI5Converter-0.0.1.jar -x $xmlFolder/articles -t articles -i $articleList -w $filename -o $articleOutput -inf ../$inflec -e $encoding > logs/$articleLog 2>&1
    fi
        
    echo "Validating I5 Wiki articles with xmllint"
    xmllint -valid -stream -dtdvalid $dtd $articleOutput > logs/i5-$prefix-articles-xmllint-validation.log 2>&1    
    
    echo "Validating I5 Wiki articles with onsgmls"
    onsgmls -E0 -wxml -s -c /usr/share/sgml/xml.soc $articleOutput > logs/i5-$prefix-articles-onsgmls-validation.log 2>&1   
	        
else

    echo "Converting $lang-wiki talk pages to XML ..." 
    java -jar target/WikiXMLConverter-0.0.1-jar-with-dependencies.jar -l $lang -w $wiki -t discussions -o $xmlFolder > logs/$wikiToXMLlog 2>&1

    discussionList=$xmlFolder/discussionList.xml
    discussionOutput=i5/$prefix-discussions.i5
    discussionLog=i5-$prefix-discussions.log
        
    echo "Listing discussions by index ..."    
    createList discussions $discussionList $xmlFolder
    
    echo "Converting $lang XML discussions to XCES ..."    
    
    if [ -z "$inflec" ]
    then
	java -Xmx10g -jar target/WikiI5Converter-0.0.1.jar -x $xmlFolder/discussions -t discussions -i $discussionList -w $filename -o $discussionOutput -e $encoding> logs/$discussionLog 2>&1
    else
	java -Xmx10g -jar target/WikiI5Converter-0.0.1.jar -x $xmlFolder/discussions -t discussions -i $discussionList -w $filename -o $discussionOutput -inf ../$inflec -e $encoding > logs/$discussionLog 2>&1
    fi

    echo "Validating I5 Wiki discussions with xmllint"
    xmllint -valid -stream -dtdvalid $dtd $discussionOutput > logs/i5-$prefix-discussions-xmllint-validation.txt 2>&1
    
    echo "Validating I5 Wiki discussions with onsgml"
    onsgmls -E0 -wxml -s -c /usr/share/sgml/xml.soc $discussionOutput > logs/i5-$prefix-discussions-onsgmls-validation.log 2>&1
    
fi
    
echo "Done."

