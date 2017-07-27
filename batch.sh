# Run at the root folder
# Example command: ./code/batch.sh de article 20150501

wikixml="code/WikiXMLConverter-1.0.2-jar-with-dependencies.jar"

lang=$1
type=$2
wiki=wiki
date=$3
filename=$lang$wiki-$date-$type
properties=code/properties/$lang/xml-$lang$wiki-$type.properties

# create some directories if they don't not exists
mkdir -p logs
mkdir -p post

echo "Converting Wikitext to WikiXML"
echo $filename
echo $properties
nice -n 3 java -jar -Xmx4g $wikixml -prop $properties > logs/wikiXML-$filename.log 2>&1
