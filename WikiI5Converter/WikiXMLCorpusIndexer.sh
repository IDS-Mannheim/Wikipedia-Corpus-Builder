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
# <wiki type="article">
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
# </wiki>
#
# The index is used in WikiI5Converter for structuring the Wikipedia
# pages in the resulting Wikipedia I5 corpus file.
#
# Eliza Margaretha
# Institut für Deutsche Sprache
#
# Created: Juli 2015
# Last update: March 2019
#
# This script is licensed under GPL v3.
# -------------------------------------------------------------------

function createList {
    echo "<wiki type=\""$1"\">" > $3

    for index in $(find -L $2 -type d | sed 's/.*\///' | sort);
    do
        echo "  <index value=\""$index"\">" >> $3
        find -L $2/$index/ -type f 2>/dev/null | sed 's/.*\/\(.*\)\.xml/\1/' | sort -n | sed 's/\(.*\)/    <id>\1<\/id>/' >> $3
        echo "  </index>" >> $3
    done

    echo "</wiki>" >> $3
}


# variables

pageType=$1
xmlFolder=$2
output=$3

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ];
then
    echo "Specify the following input parameters (separated by a space):"
    echo "1. pageType e.g. article, talk, user-talk"
    echo "2. root path of a wikiXML folder, e.g: wikixml-de/article"
    echo "3. output file path"
    echo ""
    echo "Example:"
    echo "./WikiXMLCorpusIndexer.sh article wikixml-de/article article-index.xml"
    exit
fi

echo "Indexing WikiXML" $1 "pages"

dir=$(dirname $output)
mkdir -p $dir

createList $pageType $xmlFolder $output
