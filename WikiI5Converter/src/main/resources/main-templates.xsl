<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:saxon="http://saxon.sf.net/" xmlns:functx="http://www.functx.com" version="3.0"
    xmlns:err="http://www.w3.org/2005/xqt-errors"
    extension-element-prefixes="saxon" exclude-result-prefixes="xs xd saxon functx">
    

    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p>Templates for processing Wikipedia pages and grouping</xd:p>
            <xd:p>Version 4.1</xd:p>
            <xd:p><xd:b>Revision:</xd:b> Oct 2019</xd:p>
            <xd:p><xd:b>Editor:</xd:b> Eliza Margaretha</xd:p>
            
            <xd:p>Version 4.0</xd:p>
            <xd:p><xd:b>Revision:</xd:b> Aug 2019</xd:p>
            <xd:p><xd:b>Editor:</xd:b> Eliza Margaretha</xd:p>
            
            <xd:p>Version 3.0</xd:p>
            <xd:p><xd:b>Revision:</xd:b> July 2017</xd:p>
            <xd:p><xd:b>Editor:</xd:b> Eliza Margaretha</xd:p>

            <xd:p>Version 2.0</xd:p>
            <xd:p><xd:b>Revision:</xd:b> June 2013</xd:p>
            <xd:p><xd:b>Editor:</xd:b> Eliza Margaretha</xd:p>

            <xd:p><xd:b>Version 1.0 last modified:</xd:b> July 23, 2011</xd:p>
            <xd:p><xd:b>Author:</xd:b> Stefanie Haupt</xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:include href="paragraph-level-templates.xsl"/>
    <xsl:include href="phrase-level-templates.xsl"/>
    <xsl:include href="escape-element-templates.xsl"/>
    
    <xsl:param name="type" required="yes"/>
    <xsl:param name="korpusSigle" required="yes"/>
    <xsl:param name="lang" required="yes"/>
    <xsl:param name="origfilename" required="yes"/>
    <xsl:param name="pubDay" required="yes"/>
    <xsl:param name="pubMonth" required="yes"/>
    <xsl:param name="pubYear" required="yes"/>
    <xsl:param name="letter" required="yes"/>
    <xsl:param name="encoding" required="yes"/>
    <xsl:param name="pageId" required="yes"/>

    <!-- <xsl:output doctype-public="-//IDS//DTD IDS-XCES 1.0//EN" doctype-system="dtd/i5.dtd"
        method="xml" encoding="{$encoding}" indent="yes"/> -->
    <xsl:output method="xml" encoding="{$encoding}" indent="yes" saxon:indent-spaces="1" />    
	 
    <xsl:variable name="errorCounter" select="0" saxon:assignable="yes"/>
    <xsl:variable name="sigle" saxon:assignable="yes"/>
    
    <xsl:variable name="threadNum" select="0" saxon:assignable="yes"/>
    <xsl:variable name="postNum" select="0" saxon:assignable="yes"/>
	
    <xsl:param name="headerNames">
        <name>h1</name>
        <name>h2</name>
        <name>h3</name>
        <name>h4</name>
        <name>h5</name>
        <name>h6</name>
        <name>h7</name>
        <name>h8</name>
        <name>h9</name>
    </xsl:param>

    <xsl:template name="main">
        <xsl:variable name="doc">
            <xsl:copy-of saxon:read-once="yes" select="saxon:discard-document(.)"/>
        </xsl:variable>
        <xsl:apply-templates select="saxon:stream($doc/page)"/>

    </xsl:template>


    <xsl:template match="page">
        <xsl:variable name="textSigle">
			<xsl:variable name="intermediate">
				<xsl:sequence
					select="concat($korpusSigle,'/',
		                            upper-case($letter), 
		                            format-number(id, '000000000')
		                            )" />
			</xsl:variable>
			<xsl:sequence
				select="concat(substring($intermediate,1,11),'.',substring($intermediate,12))" />
		</xsl:variable>

        <saxon:assign name="sigle" select="translate($textSigle,'/','.')"/>

        <!--Current index-->
        <xsl:variable name="t.title">
            <!--why is it a sequence when the values is not even a sequence? value-of is enough-->
            <xsl:value-of select="concat($textSigle,' ')"/>
            <xsl:value-of select="title"/>
            <xsl:sequence
                select="concat(', In: Wikipedia - URL:http://', $lang ,'.wikipedia.org/wiki/')"/>
            <!-- Assume this construct may be used as a weblink, ensure working link. -->
            <xsl:value-of select="translate(title,' ','_')"/>
            <xsl:sequence select="concat(': Wikipedia, ', $pubYear)"/>
        </xsl:variable>
        
        <xsl:variable name="pageURL">
        	<xsl:sequence select="concat('http://', $lang ,'.wikipedia.org/wiki/')"/>
        	<xsl:value-of select="translate(title,' ','_')"/>
        </xsl:variable>

        <!-- * idsText * -->
        <idsText>
            <xsl:attribute name="id" select="translate($textSigle,'/','.')"/>
            <!-- Avoid spaces in attribute 'n'. Attribute 'n' carries the value of the interwiki link -->
            <xsl:attribute name="n"
                select="concat(revision/text/@lang,concat('.',translate(title,' ','_')))"/>
            <xsl:attribute name="version" select="1.0"/>

            <!-- * idsHeader * -->
            <idsHeader type="text" pattern="text" version="1.0">
                <fileDesc>
                    <titleStmt>
                        <textSigle>
                            <xsl:sequence select="$textSigle"/>
                        </textSigle>
                        <t.title assemblage="external">
                            <xsl:sequence select="$t.title"/>
                        </t.title>
                    </titleStmt>
                    <editionStmt version="0"/>
                    <publicationStmt>
                        <distributor/>
                        <pubAddress/>
                        <availability region="world" status="free">CC-BY-SA</availability>
                        <pubDate/>
                    </publicationStmt>
                    <sourceDesc Default="n">
                        <biblStruct>
                            <analytic>
                                <h.title type="main">
                                    <xsl:value-of select="title"/>
                                </h.title>
                                <h.author>
                                    <xsl:value-of select="revision/contributor/(username|ip)"/>
                                    <!-- Since there is only the ip or username of the last edit made to see, add 'u.a.' -->
                                    <xsl:text>,  u.a.</xsl:text>
                                </h.author>
                                <imprint>
									<pubPlace>
										<ref type="page_url">
											<xsl:attribute name="target"><xsl:sequence select="$pageURL" /></xsl:attribute>
										</ref>
									</pubPlace>
								</imprint>
                                <idno type="wikipedia-id"><xsl:value-of select="$pageId"/></idno>
                            </analytic>
                            <monogr>
                                <h.title type="main">Wikipedia</h.title>
                                <editor>Wikimedia Foundation</editor>
                                <edition>
                                    <further>Dump file &#34;<xsl:value-of select="$origfilename"
                                        />&#34; retrieved from http://dumps.wikimedia.org</further>
                                    <kind/>
                                    <appearance/>
                                </edition>
                                <imprint>
									<publisher>Wikipedia</publisher>
									<pubPlace>
										<ref>
											<xsl:attribute name="target">
								               <xsl:sequence select="concat('http://', $lang ,'.wikipedia.org')" />
								            </xsl:attribute>
										</ref>
									</pubPlace>
                                    <pubDate type="year">
                                        <xsl:sequence select="$pubYear"/>
                                    </pubDate>
                                    <pubDate type="month">
                                        <xsl:sequence select="$pubMonth"/>
                                    </pubDate>
                                    <pubDate type="day">
                                        <xsl:sequence select="$pubDay"/>
                                    </pubDate>
                                </imprint>
                            </monogr>
                        </biblStruct>
                        <reference type="complete" assemblage="non-automatic">
                            <xsl:sequence select="$t.title"/>
                        </reference>
                        <reference type="short" assemblage="regular">
                            <xsl:value-of select="$textSigle"/> Wikipedia; <xsl:value-of
                                select="title"/>, (Letzte Änderung <xsl:value-of
                                select="format-dateTime(revision/timestamp,'[D1].[M1].[Y0001]')"
                                />) <xsl:variable name="pubDate" select="xs:date(concat($pubYear,
                                '-',$pubMonth,'-',$pubDay))"/> <xsl:value-of select="format-date(
                                $pubDate,'[D1].[M1].[Y0001]')"/>
                        </reference>
                    </sourceDesc>
                </fileDesc>
                <encodingDesc>
                    <samplingDecl Default="n"/>
                    <editorialDecl Default="n">
                        <pagination type="no"/>
                    </editorialDecl>
                </encodingDesc>
                <profileDesc>
                    <creation>
                        <creatDate>
                            <xsl:sequence
                                select="format-dateTime(revision/timestamp, '[Y0001].[M01].[D01]')"
                            />
                        </creatDate>
                        <creatRef>(Letzte Änderung <xsl:value-of
                                select="format-dateTime(revision/timestamp,'[D1].[M1].[Y0001]')"
                            />)</creatRef>
                        <creatRefShort>(Letzte Änderung <xsl:value-of
                                select="format-dateTime(revision/timestamp,'[D1].[M1].[Y0001]')"
                            />)</creatRefShort>
                    </creation>
                    <textDesc>
                        <xsl:choose>
                            <xsl:when test="$type eq 'article'">
                                <textTypeArt>Enzyklopädie-Artikel</textTypeArt>
                            </xsl:when>
                            <xsl:when test="$type eq 'talk'">
                                <textTypeArt>Diskussion</textTypeArt>
                            </xsl:when>
                            <xsl:when test="$type eq 'user-talk'">
                                <textTypeArt>Benutzerdiskussion</textTypeArt>
                            </xsl:when>
                            <xsl:when test="$type eq 'loeschkandidaten'">
                                <textTypeArt>Löschkandidaten</textTypeArt>
                            </xsl:when>
                            <xsl:when test="$type eq 'redundanz'">
                                <textTypeArt>Redundanzdiskussion</textTypeArt>
                            </xsl:when>
                            <xsl:otherwise>
                                <textTypeArt>Unerkannt</textTypeArt>
                            </xsl:otherwise>
                        </xsl:choose>
                        <textDomain/>
                    </textDesc>
                </profileDesc>
            </idsHeader>

            <!-- * text * -->
            <text>
                <!-- front always empty -->
                <front/>
                <body>
                    <xsl:apply-templates select="revision"/>
                </body>
                <!-- back contains foot note-->
                <back/>
            </text>
        </idsText>
    </xsl:template>

    <xsl:template match="revision">
        <xsl:try>
            <xsl:apply-templates select="text"/>
            <xsl:catch>
                <saxon:assign name="errorCounter" select="$errorCounter+1"/>
                <xsl:message terminate="yes">
                    <xsl:text>Id: </xsl:text>
                    <xsl:value-of select="../id"/>
                    <xsl:text>&#10;Title: </xsl:text>
                    <xsl:value-of select="../title"/><xsl:text>&#10;Error code: </xsl:text>
                    <xsl:value-of select="$err:code"/><xsl:text>&#10;Reason: </xsl:text>
                    <xsl:value-of select="$err:description"/>
                </xsl:message>

            </xsl:catch>
        </xsl:try>
    </xsl:template>

    <xsl:template match="text">
        <xsl:if test="parent::node()[name()='revision']">
            <!-- Start of the first section, level 0 -->
            <div n="0" type="section">
                <xsl:call-template name="section">
                    <xsl:with-param name="input" select="*"/>
                </xsl:call-template>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="section">
        <xsl:param name="input"/>
        <!-- Group input by header level -->
        <xsl:choose>
            <xsl:when test="$input[name() eq 'h1']">
                <xsl:for-each-group select="$input" group-starting-with="h1">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h1'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h2']">
                <xsl:for-each-group select="$input" group-starting-with="h2">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h2'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h3']">
                <xsl:for-each-group select="$input" group-starting-with="h3">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h3'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h4']">
                <xsl:for-each-group select="$input" group-starting-with="h4">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h4'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h5']">
                <xsl:for-each-group select="$input" group-starting-with="h5">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h5'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h6']">
                <xsl:for-each-group select="$input" group-starting-with="h6">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h6'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h7']">
                <xsl:for-each-group select="$input" group-starting-with="h7">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h7'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h8']">
                <xsl:for-each-group select="$input" group-starting-with="h8">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h8'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:when test="$input[name() eq 'h9']">
                <xsl:for-each-group select="$input" group-starting-with="h9">
                    <xsl:call-template name="group">
                        <xsl:with-param name="groupingKey" select="'h9'"/>
                    </xsl:call-template>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="paragraphLevel">
                    <xsl:with-param name="input" select="$input"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="group">
        <xsl:param name="groupingKey"/>
        <xsl:choose>
            <!-- Group starting with header element -->
            <xsl:when test="name() eq $groupingKey">
                <xsl:apply-templates select="."/>
            </xsl:when>
            <!-- Group containing header element -->
            <xsl:when test="current-group()[name()=$headerNames/name]">
                <xsl:call-template name="section">
                    <xsl:with-param name="input" select="current-group()"/>
                </xsl:call-template>
            </xsl:when>
            <!-- Group without header element -->
            <xsl:otherwise>
                <xsl:call-template name="paragraphLevel">
                    <xsl:with-param name="input" select="current-group()"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="paragraphLevel">
        <xsl:param name="input"/>
        <xsl:for-each select="$input">
            <xsl:if test="text()[normalize-space(.)] | *">
                <xsl:choose>
                    <!-- Handling header inside invalid elements : Kompliciert discussion D/5372030.xml-->
                    <xsl:when
                        test=".[not(name()=('dl','ul','ol')) and descendant::*[name()=$headerNames/name]]">
                        <xsl:call-template name="section">
                            <xsl:with-param name="input" select="*"/>
                        </xsl:call-template>
                    </xsl:when>
                    <!-- Handling non-paragraph elements-->
                    <!-- also var-->
                    <xsl:when
                        test="name()=('text','table','span','a','i','b','strong','u',
                        'small','big','sup','sub','tt','font','syntaxhighlight','Syntaxhighlight')">
                        <p>
                            <xsl:apply-templates select="."/>
                        </p>
                    </xsl:when>
                    <!-- Paragraph elements: paragraph, list, poem, quote, etc. -->
                    <xsl:otherwise>
                        <xsl:apply-templates select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:variable name="headingType">
        <xsl:choose>
            <xsl:when test="$type eq 'article'">section</xsl:when>
            <xsl:otherwise>thread</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:template match="h1|h2|h3|h4|h5|h6|h7|h8">
        <xsl:choose>
            <xsl:when test="parent::node()[name() = ('dd','li','dt','ul','ol','dl')]">
                <xsl:if test="parent::node()[1]=.">
                    <xsl:call-template name="heading"/>
                </xsl:if>
            </xsl:when>
            <xsl:when test="parent::node()[name()='text']">
                <div n="{(substring(name(), 2))}" type="{$headingType}">
                	<saxon:assign name="threadNum" select="$threadNum+1"/>
                	<saxon:assign name="postNum" select="0"/>
                    <xsl:call-template name="heading"/>
                    <!-- Continue processing elements in this header scope -->
                    <xsl:call-template name="section">
                        <xsl:with-param name="input" select="current-group() except ."/>
                    </xsl:call-template>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <!-- Define header level and write header -->
                <div n="{(substring(name(), 2))}" type="{$headingType}">
                	<saxon:assign name="threadNum" select="$threadNum+1"/>
                	<saxon:assign name="postNum" select="0"/>
                    <xsl:call-template name="heading"/>
                    <!-- Continue processing elements in this header scope -->
                    <xsl:call-template name="section">
                        <xsl:with-param name="input" select="current-group() except ."/>
                    </xsl:call-template>
                </div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="heading">
        <!-- <head type="cross"> -->
        <head>
            <xsl:for-each select="child::node()">
                <xsl:choose>
                    <xsl:when test="name() eq 'br'"/>
                    <xsl:when test="name() eq 'p'">
                        <xsl:apply-templates/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </head>
    </xsl:template>

</xsl:stylesheet>
