#!/bin/bash
# ------------------------------------------------------------------------------------------
# WIKIPEDIA CONVERSION
#
# Eliza Margaretha, Mar 2013
# Institut für Deutsche Sprache
#
# This script runs the conversion of wikipedia dumps containing wiki mark-ups (i.e. wikitext) 
# into wikipedia corpus in XCES format.
#
# The conversion is done in two stages. In the first stage, each wikipage is converted into 
# an XML file. In the second stage, each XML is converted into XCES and collected into one 
# corpus file.
#
# This script takes input arguments as follows:
#  - First argument is the language of the wikitext 
#  - Second argument is the location of the wikidumps
#  - Third argument is the option to split the XML output or not [ split | nosplit ]
#
# The outputs of the first conversion are grouped by an alphabeth or a digit, and written in 
#  - xml/articles for article pages
#  - xml/discussions for discussion pages
#
# The article pages are listed in xml/articleList.xml and the discussion pages in 
# xml/discussionList.xml.
# 
# The XSLT parameters required to run the second conversion are:
# - wikidump = the filename of wikipedia dumps, in this format: [lang]wiki-[date]-pages-meta-current.xml
# - type = articles or discussions
# - xmlDir = the location of the xml files, e.g. ../xml/articles
# - list = the location of the list of the xml files e.g. ../xml/articleList.xml
#
# Note: XML file locations are relative to the location of the xsl file.
#
# The XSLT processor used is Saxon-EE 9.4.0.3 version.
#
# By default, all conversion logs are written in the parent directory.
# --------------------------------------------------------------------------------------------

# Function

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

# --------------------------------------------------------------------------------------------
# Set variables

pageType=$1
lang=$2
wiki=$3
inflec=$4

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
    echo "Please specify the file containing the list of inflectives : "
    read wiki
fi


if [ ! -d "xces" ];
then
    mkdir xces
fi 

if [ ! -d "logs" ];
then
    mkdir logs
fi 


w=wiki
filename=$(basename "$wiki")
prefix=${filename%-pages*}
xmlFolder=xml-$lang 
wikiToXMLlog=xml-$prefix-$pageType.log
dtd=dtd/i5.xlint.dtd

if [ "$pageType" == "articles" ] 
then
    
    echo "Converting Wiki articles to XML ..." 
    java -jar target/WikiXMLConverter-0.0.1-SNAPSHOT.jar $lang $wiki 0 null > logs/$wikiToXMLlog 2>&1 

    articleList=$xmlFolder/articleList.xml
    articleOutput=xces/$prefix-articles.xces
    articleLog=xces-$prefix-articles.log
    
    echo "Listing articles by index ..."    
    createList articles $articleList $xmlFolder
    
    echo "Converting XML articles to XCES ..."    
    java -Xmx10g -jar target/WikiXCESConverter-0.0.1-SNAPSHOT-jar-with-dependencies.jar $xmlFolder articles $filename $articleOutput ../$inflec ../$dtd > logs/$articleLog 2>&1     
        
    echo "Validating XCES Wiki articles with xmllint"
    xmllint -valid -stream -dtdvalid $dtd $articleOutput > logs/xces-$prefix-articles-xmllint-validation.log 2>&1    
    
    echo "Validating XCES Wiki articles with onsgmls"
    /vol/work/kupietz/OpenSP-1.5.1/OpenSP-1.5.1/nsgmls/onsgmls -E0 -wxml -s -c /usr/share/sgml/xml.soc $articleOutput > logs/xces-$prefix-articles-onsgmls-validation.log 2>&1   
        
else 

    echo "Converting Wiki talk pages to XML ..." 
    java -jar target/WikiXMLConverter-0.0.1-SNAPSHOT.jar $lang $wiki null 1 > logs/$wikiToXMLlog 2>&1

    dicussionList=$xmlFolder/discussionList.xml
    discussionOutput=xces/$prefix-discussions.xces
    discussionLog=xces-$prefix-discussions.log
        
    echo "Listing discussions by index ..."    
    createList discussions $dicussionList $xmlFolder
    
    echo "Converting XML discussions to XCES ..."    
    java -Xmx10g -jar target/WikiXCESConverter-0.0.1-SNAPSHOT-jar-with-dependencies.jar $xmlFolder discussions $filename $discussionOutput ../$inflec ../$dtd > logs/$discussionLog 2>&1  
    
    echo "Validating XCES Wiki discussions with xmllint"
    xmllint -valid -stream -dtdvalid $dtd $discussionOutput > logs/xces-$prefix-discussions-xmllint-validation.txt 2>&1
    
    echo "Validating XCES Wiki discussions with onsgml"
    /vol/work/kupietz/OpenSP-1.5.1/OpenSP-1.5.1/nsgmls/onsgmls -E0 -wxml -s -c /usr/share/sgml/xml.soc $discussionOutput > logs/xces-$prefix-discussions-onsgmls-validation.log 2>&1
    
fi
    
echo "Done."

