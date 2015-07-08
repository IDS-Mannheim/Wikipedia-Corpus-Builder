# WikiXML Corpus Indexer
# -------------------------------------------------------------------
# This script lists all the WikiXML files, that are outputs of the 
# WikiXMLConverter tool. The WikiXMLConverter generates WikiXML files 
# under xml-[language] folder. Moreover, the WikiXML pages are grouped 
# numerically and alphabetically.
# 
# Input parameters:
# - pageType [e.g. article, talk, user-talk]
# - path to xmlFolder, for example: wikixml-de/article to index the 
#   German WikiXML article pages
# - output file
#
# Example:
# ./WikiXMLCorpusIndexer.sh article wikixml-de/article article-index.xml
#
# Output format:
# <article>
#    <index value=0>
#         <id>2173</id>
#	  <id>2435</id>
#         [....]
#    </index>
#    [....]
#    <index value=A>
#         <id>1</id>
#	  <id>3</id>
#         [....]
#    </index>
#    [....]
# </article>
#
# The index is used in WikiI5Converter for structuring the Wikipedia 
# pages in the resulting Wikipedia I5 corpus file.
#
# Eliza Margaretha, Juli 2015
# Institut für Deutsche Sprache
# This script is licensed under GPL v3.
# -------------------------------------------------------------------

function createList {
    echo "<"$1">" > $3
    
    for index in $(find $2 -type d | sed 's/.*\///' | sort);
    do  
        echo "  <index value=\""$index"\">" >> $3 
        find $2/$index/ -type f 2>/dev/null | sed 's/.*\/\(.*\)\.xml/\1/' | sort -n | sed 's/\(.*\)/    <id>\1<\/id>/' >> $3        
        echo "  </index>" >> $3
    done

    echo "</"$1">" >> $3
}


# variables

pageType=$1
xmlFolder=$2
output=$3

if [ -z "$1" ];
then
    echo "Please specify the Wikipage type:  "
    read pageType
fi

if [ -z "$2" ];
then
    echo "Please specify the WikiXML folder: "
    read xmlFolder
fi

if [ -z "$3" ];
then
    echo "Please specify the output file name: "
    read output
fi



echo "Indexing WikiXML" $1 "pages"    
createList $pageType $xmlFolder $output
