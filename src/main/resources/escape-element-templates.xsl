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
    
    <xsl:param name="inflectives"/>
    
    <xsl:param name="inflectiveNames">
        <xsl:if test="$inflectives">
            <xsl:copy-of select="doc($inflectives)//inflectives//name"/>
        </xsl:if>
    </xsl:param>
    
    <xsl:variable name="inflectiveCounter" select="0" saxon:assignable="yes"/>
    
    <!-- Escaped Element Templates -->
    
    <xsl:template match="s | strike | del">
        <xsl:call-template name="esc">
            <xsl:with-param name="name" select="name()"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="node()[name()=$inflectiveNames/*]">
        <saxon:assign name="inflectiveCounter" select="$inflectiveCounter+1"/>
        <xsl:variable name="openNum" select="$inflectiveCounter"/>
        <xsl:variable name="openId" select="concat($sigle,'-',$openNum,'-iaw',$openNum)"/>
        
        <saxon:assign name="inflectiveCounter" select="$inflectiveCounter+1"/>
        <xsl:variable name="closeNum" select="$inflectiveCounter"/>
        <xsl:variable name="closeId" select="concat($sigle,'-',$closeNum,'-iaw',$closeNum)"/>
        
        <xsl:element name="interactionTerm">
            <xsl:element name="interactionWord">
                <xsl:attribute name="id" select="$openId"/>
                <xsl:attribute name="n" select="$openNum"/>
                <xsl:attribute name="next" select="$closeId"/>
                <xsl:attribute name="topology">openingTag</xsl:attribute>
                <xsl:value-of select="concat('&lt;',name(),'&gt;')"/>
            </xsl:element>
        </xsl:element>
        <xsl:call-template name="esc">
            <xsl:with-param name="name" select="name()"/>
        </xsl:call-template>
        <xsl:element name="interactionTerm">
            <xsl:element name="interactionWord">
                <xsl:attribute name="id" select="$closeId"/>
                <xsl:attribute name="n" select="$closeNum"/>
                <xsl:attribute name="prev" select="$openId"/>
                <xsl:attribute name="topology">closingTag</xsl:attribute>
                <xsl:value-of select="concat('&lt;',name(),'&gt;')"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:variable name="name">
            <xsl:choose>
                <xsl:when test="name() eq 'div'">
                    <xsl:value-of select="name()"/>-<xsl:choose>
                        <xsl:when test="not(empty(@class))">
                            <xsl:value-of select="@class"/>
                        </xsl:when>
                        <xsl:otherwise>other</xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="name()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="esc">
            <xsl:with-param name="name" select="$name"/>
        </xsl:call-template>
        
    </xsl:template>
    
    <xsl:template name="esc">
        <xsl:param name="name"/>
        <xsl:if test="text()[normalize-space(.)] | *">
            <xsl:variable name="value">
                <xsl:value-of select="."/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when
                    test="parent::node()[name()='div' and @class = ('tickerList','toccolours')]"/>
                <xsl:when test="parent::node()[name()='div' and @class = 'thumbcaption']">
                    <xsl:copy-of select="$value"/>
                </xsl:when>
                <xsl:when test="parent::node()[name()=('text','center','div')]">
                    <p>
                        <xsl:copy-of select="$value"/>
                    </p>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$value"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>