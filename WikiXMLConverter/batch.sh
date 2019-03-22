# Run at the root folder
# Example command: ./batch.sh de article 20130728

wikixml="target/WikiXMLConverter-1.0.3-jar-with-dependencies.jar"

lang=$1
type=$2
wiki=wiki
date=$3
filename=$lang$wiki-$date-$type
properties=src/test/resources/$lang$wiki-$type.properties

# create some directories if they don't not exists
mkdir -p logs/wikiXML/$lang
mkdir -p post

echo "Converting Wikitext to WikiXML"
echo $filename
echo $properties
nice -n 3 java -jar -Xmx4g $wikixml -prop $properties > logs/wikiXML/$lang/wikiXML-$filename.log 2>&1
