

# Run at the root project directory
# Example command: ./WikiI5Runner.sh de article 20150501
# ---------------------------------------------------------------------

function show_help {
    echo "WikiI5Runner"
    echo ""
    echo "A script to sequentially running WikiXMLIndexer and WikiI5Converter validating the output with xmllint or saxon."
    echo ""
    echo "./WikiI5Runner.sh [OPTIONS] [2-letter language code] [page type] [dump date]"
    echo ""
    echo "For example, to generate WikiXMLIndex and run WikiI5Converter with --storeCategoriesoption:"
    echo "     ./WikiI5Runner.sh -ic de article 20150501"
    echo ""
    echo "OPTIONS"
    echo ""
    echo "-h print help"
    echo "-i run WikiXMLCorpusIndexer generating index of WikiXML pages"
    echo "-c run WikiI5Converter with --storeCategories option which is only required once for each article corpus"
    echo "-x validate using xmllint"
    echo "-s validate using saxon"
    exit
}



wikiI5="target/WikiI5Converter-1.0.6.jar"
indexWikiXML=false
storeCategories=false
useXmllint=false
useSaxon=false

# A POSIX variable
OPTIND=1         # Reset in case getopts has been used previously in the shell.

while getopts "hicxs" opt; do
  case "$opt" in
    h)
      show_help
      exit 0
      ;;
    i)  indexWikiXML=true
      ;;
    c)  storeCategories=true
      ;;
    x)  useXmllint=true
      ;;
    s)  useSaxon=true
      ;;  
  esac
done

shift $((OPTIND-1))

[ "${1:-}" = "--" ] && shift

lang=$1
type=$2
wiki=wiki
date=$3
filename=$lang$wiki-$date-$type

mkdir -p logs/wikiI5/$lang;

mkdir -p index;

mkdir -p i5/$1

if [ -z $1 ]||[ -z $2 ]||[ -z $3 ];
then
    show_help
    exit 0
fi

if [ "$indexWikiXML" = true ];
then
    #echo "Indexing wikixml files"
    ./WikiXMLCorpusIndexer.sh $type wikixml-$lang/$type/ index/$lang$wiki-$type-index.xml
fi

echo "Converting wikiXML to I5"
main=de.mannheim.ids.wiki.WikiI5Converter
#prop=code/properties/$lang/i5-$lang$wiki-$type.properties
prop=src/test/resources/$lang$wiki-$type.properties
echo $prop
lib=:lib/*:.

if [ "$storeCategories" = true ];
then
    echo "Storing categories to database"
    nice -n 3 java -Xmx4g -cp $wikiI5$lib $main -prop $prop -storeCategories > logs/wikiI5/$lang/wikiI5-$filename.log 2>&1

else
    nice -n 3 java -Xmx4g -cp $wikiI5$lib $main -prop $prop > logs/wikiI5/$lang/wikiI5-$filename.log 2>&1 
fi 

#echo "Replacing invalid Chars"
 #sed -i -e 's/&#xd[8-9a-f][0-9a-f][0-9a-f];/ /g' i5/$filename.i5.xml; 
#perl -wlnpe 's/\&#xd[89a-f]..;/\&#xf8ff;/g' < i5/ori/$filename.i5.xml > i5/$lang/$filename.i5.xml

if [ "$useSaxon" = true ];
then
    echo "Validating using saxon"
    mkdir -p logs/saxon/$lang;
    saxon-validate i5/$lang/$filename.i5.xml > logs/saxon/$lang/saxon-$filename.i5.log 2>&1;
fi

if [ "$useXmllint" = true ];
then
    echo "Validating using xmllint"
    mkdir -p logs/xmllint/$lang;
    xmllint -huge -valid -stream i5/$lang/$filename.i5.xml > logs/xmllint/xmllint-$filename.i5.log 2>&1; 
fi

#echo "Validating using onsgmls"
#mkdir -p logs/onsgmls/$lang;
#onsgmls -E0 -wxml -s -c /usr/share/sgml/xml.soc i5/$lang/$filename.i5.xml > logs/onsgmls/onsgmls-$filename.i5.log 2>&1

