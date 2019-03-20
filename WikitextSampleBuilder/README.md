# Wikitext sample builder

Wikitext sample builder is a simple tool to randomly extract a number of Wikipages from a Wikipedia dump as a sample. The number of Wikipages or the size of the sample is determined by a constant factor with value between 0 and 1. The smaller the value, the smaller the number of pages included in the sample. 

```java -jar WikitextSampleBuilder-[version].jar -i [input-file] -o [output-file] -f [factor] -lw [Löschkandidaten page factor] -rw [redundanz page factor]``` 

where ```i``` is a required option and the other options are optional. By default the factor is set to 0.00001. 

In addition to the factor parameter, there are two weight parameters ```lw``` and ```rw``` influencing the inclusion of Wikipedia:Löschkandidaten and Wikipedia:Redundanz pages respectively. Like the factor parameter, the weight values range from 0 to 1. Setting the weight values increase the chance of these pages to be included in the sample. By default both weights are set to 0.