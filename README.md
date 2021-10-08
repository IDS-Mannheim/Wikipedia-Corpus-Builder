# Wikipedia Corpus Builder

The [Leibniz-Institut für Deutsche Sprache (IDS)](http://www1.ids-mannheim.de/) develops a corpus builder for Wikipedia, that converts Wikipedia pages from its native text format, Wikitext, into our target corpus format, I5. I5 is the IDS text model used in [Das Deutsche Referenzkorpus (DeReKo)](http://www1.ids-mannheim.de/kl/projekte/korpora/). It is a customized TEI format based on XCES, enriched with metadata information on different corpus structure levels (Lüngen and Sperberg-McQueen, 2012). As part of DeReKo, Wikipedia corpora built using this tool, are accessible through [Corpus Search, Management and Analysis System II (COSMAS II)](http://www.ids-mannheim.de/cosmas2/) and [Corpus Analysis Platform (KorAP)](https://korap.ids-mannheim.de/).

The corpus builder works in two stages of conversion (Margaretha and Lüngen, 2014). In the first stage, WikiXMLConverter converts Wikitext into WikiXML by using [Sweble Parser](https://osr.cs.fau.de/research/research-resources/sweble-wikitext/) and generates a WikiXML file for each wikipage within a Wikipedia namespace, for instance articles. In the second stage, WikiI5Converter converts each WikiXML file into I5 using XSLT Stylesheets and assemble them altogether as a single corpus file as required for DeReKo.

The corpus builder is also designed for building Computer Mediated Communication (CMC) corpora from Wikipedia talk or discussion pages, such as in the Talk and User talk [namespaces](https://en.wikipedia.org/wiki/Wikipedia:Namespace). A talk corpus is structured by postings and threads following the TEI scheme for CMC corpus (Beißwenger, et al., 2012). Our posting segmentation is done heuristically in WikiXMLConverter.

The corpus builder supports parsing Wikipedia of multiple languages. It has been tested for the following languages: english, french, hungarian, norwegian, spanish, croatian, italian, polish and rumanian. We also provide Wikipedia corpora of these languages in WikiXML and I5 formats for [download](https://www.ids-mannheim.de/digspra/kl/projekte/korpora/verfuegbarkeit-1/).

## References:

Eliza Margaretha, Harald Lüngen (2014): [Building linguistic corpora from Wikipedia articles and discussions.](http://www.jlcl.org/2014_Heft2/3MargarethaLuengen.pdf) In: Journal for Language Technologie and Computational Linguistics (JLCL) 2/2014

Michael Beißwenger, Maria Ermakova, Alexander Geyken, Lothar Lemnitzer, Angelika Storrer (2012): [A TEI Schema for the Representation of Computer-mediated Communication.](https://doi.org/10.4000/jtei.476) In: Journal of the Text Encoding Initiative (jTEI) 3/2012
