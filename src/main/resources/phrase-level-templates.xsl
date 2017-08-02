<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:saxon="http://saxon.sf.net/" xmlns:functx="http://www.functx.com" version="3.0"
    extension-element-prefixes="saxon" exclude-result-prefixes="xs xd saxon functx">

    <xd:doc scope="stylesheet">
        <xd:desc>TES <xd:p>Templates for various elements</xd:p>
            <xd:p><xd:b>Date:</xd:b> June 2013</xd:p>
            <xd:p><xd:b>Author:</xd:b> Eliza Margaretha</xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:variable name="refCounter" select="1" saxon:assignable="yes"/>

    <!-- Posting Template -->
    <xsl:template match="posting">
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:element name="posting">
                <xsl:attribute name="indentLevel" select="@indentLevel"/>
                <xsl:if test="@who">
                    <xsl:variable name="author" select="@who"/>
                    <xsl:attribute name="who" select="$author"/>
                </xsl:if>
                <xsl:if test="@synch">
                    <xsl:variable name="timestamp" select="@synch"/>
                    <xsl:attribute name="synch" select="$timestamp"/>
                </xsl:if>

                <xsl:for-each select="*">
                    <xsl:choose>
                        <xsl:when test="name()=('p')">
                            <p>
                                <xsl:apply-templates/>
                            </p>
                        </xsl:when>
                        <xsl:when test="name()=('div','poem','dl','ul','ol','table','gap','seg')">
                            <xsl:apply-templates select="."/>
                        </xsl:when>
                        <xsl:when test="name()=('pre','center','blockquote','strike','s')">
                            <p>
                                <xsl:value-of select="."/>
                            </p>
                        </xsl:when>
                        <xsl:otherwise>
                            <p>
                                <xsl:apply-templates/>
                            </p>
                            <!--  xsl:message>rest <xsl:copy-of select="."/-->
                            <!--                            </xsl:message>-->
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>


    <!-- Phrase Level Templates -->


    <xsl:template match="text()" priority="2">
        <xsl:value-of select="normalize-space(.)"/>
        <xsl:if
            test="substring(., string-length(.)) = ' ' and substring(., string-length(.) - 1, string-length(.)) != '  '">
            <xsl:text> </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="autoSignature">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="seg">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="ref|Ref|REF">
        <ptr rend="ref" targType="note" targOrder="u" target="{$sigle}-{$refCounter}"/>
        <note id="{$sigle}-{$refCounter}" place="foot">
            <xsl:apply-templates/>
        </note>
        <saxon:assign name="refCounter" select="$refCounter+1"/>
    </xsl:template>


    <xsl:template match="table">
        <gap desc="table" reason="omitted"/>
    </xsl:template>

    <xsl:template match="tr|td|th"/>

    <xsl:template match="timeline | references | References | gallery | Gallery">
        <xsl:call-template name="gap">
            <xsl:with-param name="name" select="lower-case(name())"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="gap">
        <xsl:param name="name"/>
        <xsl:choose>
            <xsl:when test="parent::node()/name()=('text','center','div')">
                <p>
                    <gap desc="{$name}" reason="omitted"/>
                </p>
            </xsl:when>
            <xsl:when test="parent::node()[name()=$phraseNames/*]">
                <xsl:variable name="value">
                    <gap desc="{$name}" reason="omitted"/>
                </xsl:variable>
                <xsl:call-template name="phrase">
                    <xsl:with-param name="value" select="$value"/>
                </xsl:call-template>
            </xsl:when>
            <!--  blockquote invalid-->
            <xsl:when test="parent::node()[name()=('p', 'li', 'dd', 'dt','posting')]">
                <gap desc="{$name}" reason="omitted"/>
            </xsl:when>
            <xsl:otherwise/>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="span">
        <xsl:choose>
            <xsl:when test="@class eq 'tag-extension'">
                <gap desc="{@id}" reason="omitted"/>
            </xsl:when>
            <xsl:when test="@class eq 'template'">
                <gap desc="template" reason="omitted"/>
            </xsl:when>
            <xsl:when test="@class eq 'signature'">
                <!-- xsl:message>signature</xsl:message-->
            </xsl:when>
            <!--<xsl:when test="@class eq 'unknown-node'">                
                <gap desc="{@name}" reason="omitted"/>
            </xsl:when>  -->
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="abbr">
        <abbr>
            <xsl:value-of select="."/>
        </abbr>
    </xsl:template>

    <xsl:template match="br">
        <lb/>
    </xsl:template>

    <xsl:template match="a">
        <xsl:variable select="@href" name="url"/>
        <ref target="{$url}" targOrder="u">
            <xsl:value-of select="text()"/>
        </ref>
    </xsl:template>

    <xsl:template match="b | strong">
        <xsl:if test="text()[normalize-space(.)] | *">
            <hi rend="bo">
                <xsl:apply-templates/>
            </hi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="i">
        <xsl:if test="text()[normalize-space(.)] | *">
            <hi rend="it">
                <xsl:apply-templates/>
            </hi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="u">
        <xsl:if test="text()[normalize-space(.)] | *">
            <hi rend="ul">
                <xsl:apply-templates/>
            </hi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="small | big">
        <xsl:if test="text()[normalize-space(.)] | *">
            <hi rend="pt">
                <xsl:apply-templates/>
            </hi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="sup">
        <xsl:if test="text()[normalize-space(.)] | *">
            <hi rend="super">
                <xsl:apply-templates/>
            </hi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="sub">
        <xsl:if test="text()[normalize-space(.)] | *">
            <hi rend="sub">
                <xsl:apply-templates/>
            </hi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="tt">
        <xsl:if test="text()[normalize-space(.)] | *">
            <hi rend="tt">
                <xsl:apply-templates/>
            </hi>
        </xsl:if>
    </xsl:template>

    <xsl:template match="em | code | source">
        <xsl:call-template name="hi">
            <xsl:with-param name="rend" select="name()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="hi">
        <xsl:param name="rend"/>
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:choose>
                <xsl:when test="parent::node()/name()='text'">
                    <p>
                        <hi rend="{$rend}">
                            <xsl:choose>
                                <xsl:when test="child::node()/name()=('p','poem','dl','ul','ol')">
                                    <xsl:value-of select="."/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </hi>
                    </p>
                </xsl:when>
                <xsl:when test="parent::node()/name()=('p','dd','dt','li',$phraseNames)">
                    <hi rend="{$rend}">
                        <xsl:apply-templates/>
                    </hi>
                </xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="font">
        <!--        <xsl:if test="text()[normalize-space(.)] | *">-->
        <hi rend="font-style">
            <xsl:apply-templates/>
        </hi>
        <!--        </xsl:if>-->
    </xsl:template>

    <xsl:template match="syntaxhighlight | Syntaxhighlight">
        <!--        <xsl:if test="text()[normalize-space(.)] | *">-->
        <hi rend="syntaxhighlight">
            <xsl:apply-templates/>
        </hi>
        <!--        </xsl:if>-->
    </xsl:template>

</xsl:stylesheet>
