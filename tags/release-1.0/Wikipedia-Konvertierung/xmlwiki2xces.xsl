<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:saxon="http://saxon.sf.net/" xmlns:functx="http://www.functx.com" 
  exclude-result-prefixes="xs xd saxon functx" version="3.0">

  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>Starting point for xmlwiki2xces conversion.</xd:p>
      <xd:p><xd:b>Revision:</xd:b> Feb, 2013</xd:p>
      <xd:p><xd:b>Editor:</xd:b> Eliza Margaretha</xd:p>

      <xd:p><xd:b>Last modified:</xd:b> Jul 23, 2011</xd:p>
      <xd:p><xd:b>Author:</xd:b> stefanie</xd:p>
    </xd:desc>
  </xd:doc>

  <!-- saxon command: 
    (saxon with high memory value e.g. -Xmx3000m) -xsl:(path to this file)/xmlwiki2xces.xsl 
    (-t if you want to see the time) -it:main filename=(path to input file(result of xmlwiki2xces.xsl)) 
    -o:(path to output including new filename)
  -->

  <xsl:include href="Modul_Funktionen.xsl"/>
<!--  <xsl:include href="Modul_AllLang.xsl"/>  -->
  <xsl:include href="Main_Template.xsl"/>
  <xsl:include href="Templates.xsl"/>
  <xsl:include href="Templates2.xsl"/>

  <!--<xsl:output doctype-public="-//IDS//DTD IDS-XCES 1.0//EN"
    doctype-system="http://corpora.ids-mannheim.de/idsxces1/DTD/ids.xcesdoc.dtd" method="xml"
    encoding="UTF-8" indent="yes"/>-->
  
  <xsl:output doctype-public="-//IDS//DTD IDS-XCES 1.0//EN"
    doctype-system="../dtd/i5-fix-m.dtd" method="xml"
    encoding="UTF-8" indent="yes"/>

  <xsl:strip-space elements="*"/>

  <!-- parameters  -->
  <xsl:param name="wikidump" required="yes"/>
  <xsl:param name="xmlDir" required="yes"/>
  <xsl:param name="type" required="yes"/>
  <xsl:param name="list" required="yes"/>
  <xsl:param name="inflectives" required="yes"/>
  <xsl:param name="authors" required="no"/>
  <xsl:param name="timeline" required="no"/>
  
  <!--  variables-->   
  <xsl:variable name="errorCounter" select="0" saxon:assignable="yes"/>
  <xsl:variable name="lang" select="substring(tokenize($wikidump, '/')[last()], 1,2)"/>
  <xsl:variable name="year" select="substring(tokenize($wikidump, '/')[last()],10,2)"/>
  <xsl:variable name="datestring" select="substring(tokenize($wikidump, '/')[last()],8,8)"/>
  <xsl:variable name="pubYear" select="substring($datestring,1,4)"/>
  <xsl:variable name="pubMonth" select="substring($datestring,5,2)"/>
  <xsl:variable name="pubDay" select="substring($datestring,7,2)"/>  
  <xsl:variable name="origfilename"
    select="concat(substring(tokenize($wikidump, '/')[last()],1,15),
    '-pages-meta-current.xml.bz2')"/>
  
      
  <xsl:variable name="korpus"
    select="if ($type eq 'discussions') then ('D') else 
    (if ($type eq 'articles') then ('P') else(''))"
    as="xs:string"/>

  <xsl:variable name="korpusSigle"
    select="concat('W',$korpus,upper-case(substring($lang,1,1)),$year)"/>

  <xsl:variable name="korpusTitel"
    select="concat('Wikipedia.', $lang, ' 20', $year, ' ', 
       if($korpus eq 'D')
       then 'Diskussionen' else 'Artikel')"/>

  <xsl:variable name="staticIdsHeader">
    <publicationStmt>
      <distributor/>
      <pubAddress/>
      <availability region="world" status="restricted"/>
      <pubDate/>
    </publicationStmt>
    <sourceDesc>
      <biblStruct Default="n">
        <monogr>
          <h.title type="main"/>
          <imprint/>
        </monogr>
      </biblStruct>
    </sourceDesc>
  </xsl:variable>

  <xsl:variable name="corpusHeader">
    <!-- Create idsHeader for the corpus. -->
    <idsHeader type="corpus" pattern="allesaußerZtg/Zschr" status="new" version="1.0"
      TEIform="teiHeader">
      <fileDesc>
        <titleStmt>
          <korpusSigle>
            <xsl:sequence select="$korpusSigle"/>
          </korpusSigle>
          <c.title>
            <xsl:value-of select="$korpusTitel"/>
          </c.title>
        </titleStmt>
        <editionStmt version="1.0"/>
        <publicationStmt>
          <distributor>Institut für Deutsche Sprache</distributor>
          <pubAddress>Postfach 10 16 21, D-68016 Mannheim</pubAddress>
          <telephone>+49 (0)621 1581 0</telephone>
          <eAddress type="www">http://www.ids-mannheim.de</eAddress>
          <eAddress type="www">http://www.ids-mannheim.de/kl/projekte/korpora/</eAddress>
          <eAddress type="email">dereko@ids-mannheim.de</eAddress>
          <availability status="restricted"> This document, the IDS-Wikipdia.<xsl:value-of
              select="$lang"/>-Corpus, is part of the Archive of General Reference Corpora at the
            IDS. It is published under the Creative Commons Attribution-ShareAlike License. See
            http://creativecommons.org/licenses/by-sa/3.0/legalcode for details. See
            http://www.ids-mannheim.de/kl/projekte/korpora/releases.html on how to refer to this
            document. </availability>
          <pubDate type="year">
            <xsl:value-of select="year-from-date(current-date())"/>
          </pubDate>
        </publicationStmt>
        <sourceDesc Default="n">
          <biblStruct Default="n">
            <monogr>
              <h.title type="main">Wikipedia</h.title>
              <h.author/>
              <editor>wikipedia.org</editor>
              <edition>
                <further> Dump file &#34;<xsl:value-of select="$origfilename"/>&#34; retrieved from
                  http://dumps.wikimedia.org </further>
                <kind/>
                <appearance/>
              </edition>
              <imprint>
                <publisher>Wikipedia</publisher>
                <pubPlace>
                  <xsl:value-of select="concat('URL:http://',$lang,'.wikipedia.org')"/>
                </pubPlace>
              </imprint>
            </monogr>
          </biblStruct>
        </sourceDesc>
      </fileDesc>
      <encodingDesc>
        <projectDesc> The XSLT stylesheets and other conversion routines that have produced this
          corpus were initially developed in a collaboration of the IDS-projects "EuroGr@mm"
          (http://www.ids-mannheim.de/gra/eurogr@mm.html), "Ausbau und Pflege der Korpora
          geschriebener Gegenwartssprache" (http://www.ids-mannheim.de/kl/projekte/korpora) and
          Stefanie Haupt. </projectDesc>
        <editorialDecl>
          <conformance> This document conforms to IDS-XCES (see
            http://www.ids-mannheim.de/kl/projekte/korpora/idsxces.html) </conformance>
        </editorialDecl>
      </encodingDesc>
      <profileDesc>
        <langUsage>
          <language>
            <xsl:attribute name="id">
              <xsl:sequence select="$lang"/>
            </xsl:attribute>
            <xsl:attribute name="usage" select="100"/>
            <xsl:choose>
              <xsl:when test="$lang='de'">
                <xsl:text>Deutsch</xsl:text>
              </xsl:when>
              <xsl:when test="$lang='fr'">
                <xsl:text>Französisch</xsl:text>
              </xsl:when>
              <xsl:when test="$lang='hu'">
                <xsl:text>Ungarisch</xsl:text>
              </xsl:when>
              <xsl:when test="$lang='it'">
                <xsl:text>Italienisch</xsl:text>
              </xsl:when>
              <xsl:when test="$lang='no'">
                <xsl:text>Norwegisch</xsl:text>
              </xsl:when>
              <xsl:when test="$lang='pl'">
                <xsl:text>Polnisch</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>Unbekannt</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </language>
        </langUsage>
        <textDesc>
          <textType>
            <xsl:choose>
              <xsl:when test="$korpus eq 'P'">
                <xsl:text>Enzyklopädie</xsl:text>
              </xsl:when>
              <xsl:when test="$korpus eq 'D'">
                <xsl:text>Diskussionen zu Enzyklopädie-Artikeln</xsl:text>
              </xsl:when>
              <xsl:otherwise>Textsorte unbekannt</xsl:otherwise>
            </xsl:choose>
          </textType>
          <textTypeRef/>
        </textDesc>
      </profileDesc>
    </idsHeader>
  </xsl:variable>


  <!-- main template -->
  <xsl:template name="main">
<!--    <xsl:call-template name="main_AllLang"/>-->
    <xsl:call-template name="main-template"/>
    <xsl:message>
      <xsl:text>Number of failed transformation: </xsl:text>
      <xsl:value-of select="$errorCounter"/>  
    </xsl:message>
  </xsl:template>
  
</xsl:stylesheet>
