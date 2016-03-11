#!/bin/bash
# ------------------------------------------------------------------------------------------
# WIKIPEDIA CONVERSION
#
# This is an example batch script that configures the Wikipedia conversion pipeline. 
# It runs a set of programs convering a wikipedia dump containing wiki mark-ups (wikitext) 
# into a wikipedia corpus in I5 format.
#
# The conversion is done in two stages. In the first stage, each wikipage is converted into 
# an XML file (WikiXML) by using the WikiXMLConverter-[version]-jar-with-dependencies.jar. 
# In the second stage, each WikiXML page is converted into I5 and collected into a single 
# corpus file by using the WikiI5Converter-1.0.1.jar. The jars are available at 
# http://corpora.ids-mannheim.de /pub/tools/.
#
# The I5 corpus is validated against IDS I5 DTD (see http://corpora.ids-mannheim.
# de/I5/DTD/i5.dtd) by using XMLlint. It is also validated against SGML by ONSGML (e.g. using 
# OpenSP-1.5.1). 
#
# This script takes input arguments sequentially as follows:   
#  1. 2-letter language code of the wikitext (e.g. de).
#  2. The type of wikipedia pages [article|talk|usertalk]. 
#  3. The date of the wikidumps.
#
# The filename of the Wikidump is supposed to be in the following format
#   [languageCode]wiki-[date]-[type]
#
# The other configuration variables have to be set in the .properties file.
# In this script, the properties files is named in the following format:
#   xml-[lang]wiki-[type].properties
#   i5-[lang]wiki-[type].properties
# and are located in "code/properties/[type]"
#
# The outputs of the first stage conversion are grouped by letters and digits, and located on: 
#  - wikixml-[lang]/article for article pages
#  - wikixml-[lang]/talk for talk pages
#
# The second stage convertion requires lists of the article and talk pages.
# WikiXMLCorpusIndexer.sh does this job. It takes 3 parameters: wiki page type, wikixml folder,
# the output file name. In this script, the output indexes are located at 
#   wikixml-[lang]/[type]-index.xml
#  
# Logs of the conversion process are located in logs/ folder.
#
#
# Eliza Margaretha, Mar 2016
# Institut für Deutsche Sprache
# --------------------------------------------------------------------------------------------


# Example command: ./WikiI5Batch.sh de article 20150501

wikixml="code/WikiXMLConverter-1.0.1-jar-with-dependencies.jar"
 
lang=$1
type=$2
wiki=wiki
date=$3
filename=$lang$wiki-$date-$type
properties=code/properties/$type/xml-$lang$wiki-$type.properties

echo "Converting wikitext to WikiXML" $filename
#echo $properties
#nice -n 3 java -jar -Xmx4g $wikixml -prop $properties > logs/wikiXML-$filename.log 2>&1

#./code/WikiXMLCorpusIndexer.sh $2 wikixml-$lang/$2/ wikixml-$lang/$2-index.xml

echo "Converting wikiXML to I5"
main=de.mannheim.ids.wiki.WikiI5Converter
prop=code/properties/$type/i5-$lang$wiki-$type.properties
#nice -n 3 java -Xmx4g -cp "code/WikiI5Converter-1.0.1-jar-with-dependencies.jar:lib/*:." $main -prop $prop > logs/wikiI5-$filename.log 2>&1 

if [ ! -d "i5/$1" ];
then
    mkdir i5/$1
fi

echo "Replacing invalid Chars"
 #sed -i -e 's/&#xd[8-9a-f][0-9a-f][0-9a-f];/ /g' i5/$filename.i5.xml; 
perl -wlnpe 's/\&#xd[89a-f]..;/\&#xf8ff;/g' < i5/$filename.i5.xml > i5/$lang/$filename.i5.xml

echo "Validating against xmllint"
xmllint -valid -stream i5/$lang/$filename.i5.xml > logs/xmllint-$filename.i5.log 2>&1; 
echo "Validating against onsgmls"
onsgmls -E0 -wxml -s -c /usr/share/sgml/xml.soc i5/$lang/$filename.i5.xml > logs/onsgmls-$filename.i5.log 2>&1

