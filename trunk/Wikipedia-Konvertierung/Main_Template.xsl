<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:ids="http://www.ids-mannheim.de/ids" xmlns:saxon="http://saxon.sf.net/"
    exclude-result-prefixes="xs xd saxon ids" version="3.0">

    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Version:</xd:b>Feb 2013</xd:p>
            <xd:p><xd:b>Author:</xd:b>Eliza Margaretha</xd:p>
            <xd:p>Modified Modul_AllLang</xd:p>

            <xd:p><xd:b>Created on:</xd:b> Jul 23, 2011</xd:p>
            <xd:p><xd:b>Author:</xd:b> Stefanie Haupt</xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:variable name="index"
        select="'A','B','C','D','E','F','G','H','I','J','K','L',
        'M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
        '0','1','2','3','4','5','6','7','8','9'"/>

    <xsl:template name="main-template">
        <!-- * idsCorpus * -->
        <idsCorpus version="1.0" TEIform="teiCorpus.2">
            <xsl:sequence select="$corpusHeader"/>

            <xsl:for-each select="$index">
                <xsl:variable name="currentIndex" select="."/>
                <xsl:variable name="pageList">
                    <xsl:choose>
                        <xsl:when test="$type eq 'articles'">
                            <xsl:copy-of
                                select="doc($list)//articles//index[@value=$currentIndex]//id"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of
                                select="doc($list)//discussions//index[@value=$currentIndex]//id"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:message>
                    <xsl:text>Index: </xsl:text>
                    <xsl:value-of select="$currentIndex"/>
                </xsl:message>

                <!-- Group docNr per 100000 documents -->
                <xsl:for-each select="0 to ids:sigdigits(xs:integer($pageList/id[last()]))">
                    <xsl:variable name="docNr" select="."/>
                    <xsl:variable name="docSigle"
                        select="concat($currentIndex, format-number($docNr, '00'))"/>
                    <xsl:message>
                        <xsl:text>Document id: </xsl:text>
                        <xsl:value-of select="$docSigle"/>
                    </xsl:message>

                    <!-- Select all pages whose rounded-id identical to docNr-->
                    <xsl:variable name="pageGroup">
                        <xsl:copy-of select="$pageList/id[ids:sigdigits(.) = $docNr]"/>
                    </xsl:variable>

                    <xsl:if test="string-length($pageGroup) > 0">

                        <!-- * idsDoc * -->
                        <idsDoc type="text" version="1.0" TEIform="TEI.2">
                            <xsl:attribute name="id">
                                <xsl:value-of
                                    select="concat(if(matches($currentIndex,'[0-9]')) then '_' else '', $docSigle)"
                                />
                            </xsl:attribute>

                            <!-- * idsHeader * -->
                            <idsHeader type="document" pattern="text" status="new" version="1.0"
                                TEIform="teiHeader">
                                <!-- create IdsHeader according to Number or Letter -->
                                <fileDesc>
                                    <titleStmt>
                                        <dokumentSigle>
                                            <xsl:value-of
                                                select="concat($korpusSigle,'/', $docSigle)"/>
                                        </dokumentSigle>
                                        <d.title>
                                            <xsl:choose>
                                                <xsl:when test="$korpus eq 'P'">
                                                  <xsl:sequence
                                                  select="concat( if (matches($currentIndex,'[0-9]')) 
                                                then (concat('Wikipedia, Anfangszahl ', $currentIndex)) 
                                                else (concat('Wikipedia, Anfangsbuchstabe ',$currentIndex)),
                                                ', Teil ', format-number($docNr, '00'))"/>

                                                </xsl:when>
                                                <xsl:when test="$korpus eq 'D'">
                                                  <xsl:sequence
                                                  select="concat( if (matches($currentIndex,'[0-9]')) 
                                                then (concat('Wikipedia, Diskussionen zu Artikeln mit Anfangszahl ', $currentIndex)) 
                                                else (concat('Wikipedia, Diskussionen zu Artikeln mit Anfangsbuchstabe ',$currentIndex)),
                                                ', Teil ', format-number($docNr, '00'))"
                                                  />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                  <xsl:text>Kein Wikipedia-Korpus</xsl:text>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </d.title>
                                    </titleStmt>
                                    <xsl:sequence select="$staticIdsHeader"/>
                                </fileDesc>
                            </idsHeader>

                            <xsl:for-each select="$pageGroup/id">
                                <xsl:variable name="pageid" select="."/>
                                <xsl:message>
                                    <xsl:text>Page-id: </xsl:text>
                                    <xsl:value-of select="$pageid"/>
                                </xsl:message>

                                <xsl:variable name="filepath"
                                    select="string-join(($xmlDir,'/', $currentIndex, '/', $pageid, '.xml'))"/>

                                <xsl:variable name="page">
                                    <xsl:copy-of saxon:read-once="yes"
                                        select="saxon:discard-document(doc($filepath))"/>
                                </xsl:variable>

                                <xsl:apply-templates select="$page">
                                    <xsl:with-param name="letter" select="$currentIndex"/>
                                </xsl:apply-templates>
                            </xsl:for-each>

                        </idsDoc>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </idsCorpus>
    </xsl:template>

</xsl:stylesheet>
