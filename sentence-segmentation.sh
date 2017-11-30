lang=$1
type=$2
wiki=wiki
date=$3
filename=$lang$wiki-$date-$type

mkdir -p logs/sentence;
mkdir -p logs/topic;

if [ -z $1 ]||[ -z $2 ]||[ -z $3 ];
then
    echo "Please specify the arguments in the following format:"
    echo ""
    echo "./sentence-segmentation.sh [2-letter language code] [page type] [dump date]"
    echo ""
    echo "For example: ./sentence-segmentation.sh de article 20150501"
    exit
fi


echo "Sentence segmentation"
/usr/local/kl/bin/XCESannotateS.pl < i5/$lang/$filename.i5.xml > i5/$lang/$filename-sentence.i5.xml 2> logs/sentence/$filename-sentence.log

echo "Validating against xmllint"
xmllint -huge -valid -stream i5/$lang/$filename-sentence.i5.xml > logs/xmllint/xmllint-$filename-sentence.log 2>&1

echo "Topic categorization"
/usr/local/kl/bin/XCESaddTopic.pl < i5/$lang/$filename.i5.xml >i5/$lang/$filename-topic.i5.xml 2> logs/topic/$filename-topic.log

echo "Validating against xmllint"
xmllint -huge -valid -stream i5/$lang/$filename-topic.i5.xml > logs/xmllint/xmllint-$filename-topic.log 2>&1

