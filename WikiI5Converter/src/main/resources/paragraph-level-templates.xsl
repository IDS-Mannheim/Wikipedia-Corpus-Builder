<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:saxon="http://saxon.sf.net/" xmlns:functx="http://www.functx.com" version="3.0"
    extension-element-prefixes="saxon" exclude-result-prefixes="xs xd saxon functx">

    <xd:doc scope="stylesheet">
        <xd:desc><xd:p>Templates for various elements</xd:p>
            <xd:p><xd:b>Date:</xd:b> Oct 2019</xd:p>
            <xd:p><xd:b>Author:</xd:b> Eliza Margaretha</xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:output name="text" indent="yes" omit-xml-declaration="yes"/>

    <xsl:param name="phraseNames">
        <name>small</name>
        <name>big</name>
        <name>a</name>
        <name>u</name>
        <name>i</name>
        <name>b</name>
        <name>strong</name>
        <name>sub</name>
        <name>sup</name>
        <name>font</name>
        <name>tt</name>
        <name>em</name>
        <name>syntaxhighlight</name>
        <name>code</name>
        <name>source</name>
        <name>span</name>
        <name>ref</name>
    </xsl:param>

    <xsl:param name="divClasses">
        <name>BoxenVerschmelzen</name>
        <name>NavFrame</name>
        <name>references-small</name>
        <name>tright</name>
        <name>sideBox</name>
    </xsl:param>

    <!-- Paragraph Level Templates -->

    <xsl:template match="p">
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:choose>
                <!-- Handle paragraph inside phrase elements -->
                <xsl:when test="parent::node()[name()=$phraseNames/*]">
                    <xsl:choose>
                        <!-- When the phrase element contains header and is escaped -->
                        <xsl:when
                            test="preceding-sibling::node()/descendant-or-self::node()/name()=$headerNames/* or
                            following-sibling::node()/descendant-or-self::node()/name()=$headerNames/*">
                            <p>
                                <xsl:apply-templates/>
                            </p>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <p>
                        <xsl:apply-templates/>
                    </p>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="blockquote">
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:choose>
                <xsl:when test="parent::node()/name() = ('p','blockquote','poem',$phraseNames/*)">
                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:otherwise>
                    <quote>
                        <xsl:apply-templates/>
                    </quote>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="poem | Poem">
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:choose>
                <xsl:when test="parent::node()/name() = ('p','poem',$phraseNames/*)">
                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:otherwise>
                    <poem>
                        <xsl:for-each select="child::node()">
                            <xsl:choose>
                                <xsl:when test="name()='p'">
                                    <l>
                                        <xsl:apply-templates/>
                                    </l>
                                </xsl:when>
                                <xsl:otherwise>
                                    <l>
                                        <xsl:apply-templates select="."/>
                                    </l>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </poem>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ul | ol | dl">
        <list type="{@type}">
            <xsl:attribute name="type" select="name()"/>
            <xsl:for-each select="child::node()">
                <xsl:if test="text()[normalize-space(.)] | *">
                    <xsl:choose>
                        <xsl:when test="name()=('li','dd','dt')">
                            <xsl:apply-templates select="."/>
                        </xsl:when>
                        <xsl:when test="name()=('p','poem','blockquote')">
                            <item>
                                <xsl:apply-templates/>
                            </item>
                        </xsl:when>
                        <!-- <xsl:when test="descendant::node()/name()=('li','dd','dt')">
                            <xsl:for-each select="descendant::node()[name()=('li','dd','dt')]">
                                <item>
                                    <xsl:apply-templates select="."/>
                                </item>
                            </xsl:for-each>                           
                        </xsl:when>-->
                        <xsl:otherwise/>
                        <!-- Other elements are skipped -->
                    </xsl:choose>
                </xsl:if>
            </xsl:for-each>
        </list>
    </xsl:template>

    <xsl:template match="li | dd | dt">
        <xsl:choose>
            <xsl:when test="child::node()/name()=$headerNames/*">
                <xsl:apply-templates select="."/>
            </xsl:when>
            <xsl:when test="child::node()/name()=('p','poem','blockquote')">
                <item>
                	<xsl:apply-templates select="child::node()"/>
                </item>
            </xsl:when>
            <xsl:when test="../name()=('small')">
            	<xsl:apply-templates select="child::node()"/>
            </xsl:when>
            <xsl:when test="../../child::node()/name()=('ul','ol','dl')">
           		<item>
                    <xsl:for-each select="child::node()">
                        <xsl:apply-templates select="."/>
                    </xsl:for-each>
                </item>
            </xsl:when>
            <xsl:when test="../../child::node()/name()=('dd','dt')">    
                <xsl:for-each select="child::node()">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </xsl:when>

            <xsl:otherwise/>
            <!-- Incorrect placements of the element are skipped. -->
        </xsl:choose>
    </xsl:template>

    <xsl:template match="div">
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:choose>
                <xsl:when test="@class='thumbcaption'">
                    <xsl:call-template name="caption"/>
                </xsl:when>
                <xsl:when test="@class=('tickerList','toccolours')">
                    <xsl:call-template name="gap">
                        <xsl:with-param name="name" select="@class"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="@class=$divClasses/*">
                    <xsl:variable name="value">
                        <div type="{@class}">
                            <xsl:call-template name="section">
                                <xsl:with-param name="input" select="*"/>
                            </xsl:call-template>
                        </div>
                    </xsl:variable>
                    <xsl:call-template name="div">
                        <xsl:with-param name="test1" select="$value"/>
                        <xsl:with-param name="test2" select="$value"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="parent::node()/name()=('blockquote')">
                    <p>
                        <xsl:value-of select="."/>
                    </p>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="div">
                        <xsl:with-param name="test1">
                            <div type="other">
                                <p>
                                    <xsl:value-of select="."/>
                                </p>
                            </div>
                        </xsl:with-param>
                        <xsl:with-param name="test2">
                            <p>
                                <xsl:value-of select="."/>
                            </p>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template name="div">
        <xsl:param name="test1"/>
        <xsl:param name="test2"/>
        <xsl:choose>
            <xsl:when test="parent::node()/name()=('text','blockquote','center','div')">
                <xsl:copy-of select="$test1"/>
            </xsl:when>
            <xsl:when
                test="preceding-sibling::node()/name()=$headerNames/* or
                following-sibling::node()/name()=$headerNames/*">
                <xsl:copy-of select="$test2"/>
            </xsl:when>
            <xsl:when test="parent::node()=('p',$phraseNames)">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise/>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="caption">
        <xsl:call-template name="caption"/>
    </xsl:template>

    <xsl:template name="caption">
        <xsl:choose>
            <xsl:when test="parent::node()/name()='table'"/>
            <xsl:when test="parent::node()/name()=('text','td','div')">
                <caption>
                    <xsl:choose>
                        <xsl:when
                            test="child::node()/name()!=('p','ul','ol','dl','poem','blockquote')">
                            <p>
                                <!-- Handling complex structures in a caption - TODO: hack version due to false conversion -->
                                <xsl:for-each select="child::node()">
                                    <xsl:choose>
                                        <xsl:when test="name()=('p','poem','div','center')">
                                            <xsl:value-of select="."/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:apply-templates select="."/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </p>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates/>
                        </xsl:otherwise>
                    </xsl:choose>
                </caption>
            </xsl:when>
            <xsl:when test="parent::node()[name()=$phraseNames/*]">
                <xsl:variable name="value">
                    <xsl:value-of select="."/>
                </xsl:variable>
                <xsl:call-template name="phrase">
                    <xsl:with-param name="value" select="$value"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="parent::node()/name()=('p','dd','dt','li')">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise/>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="phrase">
        <xsl:param name="value"/>
        <xsl:choose>
            <!-- When inside a phrase element containing a header-->
            <xsl:when
                test="preceding-sibling::node()/name()=$headerNames/* or
            following-sibling::node()/name()=$headerNames/*">
                <p>
                    <xsl:copy-of select="$value"/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$value"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="pre">
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:variable name="value">
                <xsl:value-of select="."/>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="parent::node()/name()=('text','td','th','div')">
                    <div type="{name()}">
                        <p>
                            <xsl:apply-templates/>
                        </p>
                    </div>
                </xsl:when>
                <xsl:when test="parent::node()/name()=('blockquote')">
                    <p>
                        <xsl:apply-templates/>
                    </p>
                </xsl:when>
                <xsl:when test="parent::node()[name()=$phraseNames/*]">
                    <xsl:call-template name="phrase">
                        <xsl:with-param name="value" select="$value"/>
                    </xsl:call-template>
                </xsl:when>
                <!-- parent node: p, li, dd, poem, blockquote -->
                <xsl:when test="parent::node()/name()=('p','li','dd')">
                    <xsl:copy-of select="$value"/>
                </xsl:when>
                <!-- Incorrect placement, e.g. inside an s or timeline -->
                <xsl:otherwise/>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="center">
        <xsl:choose>
            <xsl:when test="parent::node()/name()=('text','center')">
                <div type="center">
                    <xsl:call-template name="section">
                        <xsl:with-param name="input" select="*"/>
                    </xsl:call-template>
                </div>
            </xsl:when>
            <xsl:when test="parent::node()[name()='div' and @class=($divClasses/*,'thumbcaption')]">
                <div type="center">
                    <xsl:call-template name="section">
                        <xsl:with-param name="input" select="*"/>
                    </xsl:call-template>
                </div>
            </xsl:when>
            <!-- When inside another element and a header is a sibling -->
            <xsl:when
                test="preceding-sibling::node()/name()=$headerNames/* or
                following-sibling::node()/name()=$headerNames/*">
                <div type="center">
                    <xsl:call-template name="section">
                        <xsl:with-param name="input" select="*"/>
                    </xsl:call-template>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


</xsl:stylesheet>
