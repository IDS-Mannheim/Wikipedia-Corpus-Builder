# Run at the root project directory
# Example command: ./I5Batch.sh de article 20150501

wikiI5="code/WikiI5Converter-1.0.2-jar-with-dependencies.jar"
 
lang=$1
type=$2
wiki=wiki
date=$3
filename=$lang$wiki-$date-$type

mkdir -p logs/wikiI5;
mkdir -p logs/onsgmls;
mkdir -p logs/xmllint;

mkdir -p index;

mkdir -p i5/$1

if [ -z $1 ]||[ -z $2 ]||[ -z $3 ];
then
    echo "Please specify the arguments in the following format:"
    echo ""
    echo "./I5Batch.sh [2-letter language code] [page type] [dump date]"
    echo ""
    echo "For example: ./I5Batch.sh de article 20150501"
    exit
fi

#echo "Indexing wikixml files"
#./code/WikiXMLCorpusIndexer.sh $type wikixml-$lang/$type/ index/$lang$wiki-$type-index.xml

echo "Converting wikiXML to I5"
main=de.mannheim.ids.wiki.WikiI5Converter
prop=code/properties/$lang/i5-$lang$wiki-$type.properties
#prop=src/test/resources/i5-$lang$wiki-$type.properties
echo $prop
lib=:lib/*:.
nice -n 3 java -Xmx4g -cp $wikiI5$lib $main -prop $prop > logs/wikiI5/wikiI5-$filename.log 2>&1 

echo "Replacing invalid Chars"
 #sed -i -e 's/&#xd[8-9a-f][0-9a-f][0-9a-f];/ /g' i5/$filename.i5.xml; 
perl -wlnpe 's/\&#xd[89a-f]..;/\&#xf8ff;/g' < i5/ori/$filename.i5.xml > i5/$lang/$filename.i5.xml

echo "Validating against xmllint"
xmllint -valid -stream i5/$lang/$filename.i5.xml > logs/xmllint/xmllint-$filename.i5.log 2>&1; 

echo "Validating against onsgmls"
onsgmls -E0 -wxml -s -c /usr/share/sgml/xml.soc i5/$lang/$filename.i5.xml > logs/onsgmls/onsgmls-$filename.i5.log 2>&1
