<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:functx="http://www.functx.com" xmlns:ids="http://www.ids-mannheim.de/ids"
  exclude-result-prefixes="xs xd functx" version="2.0">
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p><xd:b>Created on:</xd:b> Jul 23, 2011</xd:p>
      <xd:p><xd:b>Author:</xd:b> stefanie</xd:p>
      <xd:p>Functions - credit to www.xsltfunctions.com functx:replace-multi replaces illegal sigle
        letters</xd:p>
    </xd:desc>
  </xd:doc>

  <xsl:function name="functx:replace-multi" as="xs:string?">
    <xsl:param name="arg" as="xs:string?"/>
    <xsl:param name="changeFrom" as="xs:string*"/>
    <xsl:param name="changeTo" as="xs:string*"/>

    <xsl:sequence
      select=" 
      if (count($changeFrom) > 0)
      then functx:replace-multi(
      replace($arg, $changeFrom[1],
      functx:if-absent($changeTo[1],'')),
      $changeFrom[position() > 1],
      $changeTo[position() > 1])
      else $arg
      "/>

  </xsl:function>

  <xsl:function name="functx:if-absent" as="item()*">
    <xsl:param name="arg" as="item()*"/>
    <xsl:param name="value" as="item()*"/>

    <xsl:sequence select=" 
      if (exists($arg))
      then $arg
      else $value
      "/>

  </xsl:function>


  <!-- Generate document's id starting from 0.00001-->
  <xsl:function name="ids:sigdigits">
    <xsl:param name="i"/>
    <xsl:value-of
      select="if($i castable as xs:integer) then ($i div  100000) cast as xs:integer else 0"/>
  </xsl:function>
</xsl:stylesheet>
