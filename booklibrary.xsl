<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/TR/REC-html40">
  <xsl:template match="/booklibrary">
    
    <HTML>
    <HEAD>
    <META http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    <TITLE>Book Library</TITLE>
    </HEAD>
    <BODY>
    <H1>Book Library</H1>
    <table width="100%" border="5" cellspacing="3">
      <tr>
          <th width="27%">Title</th>
          <th width="20%">Author(s)</th>
          <th width="20%">Series</th>
          <th width="8%">Publish Year</th>
          <th width="10%">Cover</th>
          <th width="15%">ISBN</th>
      </tr>
    <xsl:for-each select="book">
       <tr>
          <td width="27%"><xsl:value-of select="title"/></td>
          <td width="20%">
              <xsl:for-each select="author">
                <xsl:if test="title">
                   <xsl:value-of select="title"/>
                   <xsl:text> </xsl:text> 
                </xsl:if>
                <xsl:if test="first">
                   <xsl:value-of select="first"/>
                   <xsl:text> </xsl:text> 
                </xsl:if>
                <xsl:if test="middle">
                   <xsl:value-of select="middle"/>
                   <xsl:text> </xsl:text> 
                </xsl:if>
                <xsl:if test="last">
                   <xsl:value-of select="last"/>
                   <xsl:text> </xsl:text> 
                </xsl:if>
                <xsl:value-of select="surtitle"/>
                <xsl:if test="position()!=last()">
                   <BR/>
                </xsl:if>
              </xsl:for-each>
          </td>
          <td width="20%">
              <xsl:choose>
                 <xsl:when test="series">
                    <xsl:value-of select="series"/>
                 </xsl:when>
                 <xsl:otherwise>
                    <BR/>
                 </xsl:otherwise>
              </xsl:choose>
          </td>
          <td width="8%">
              <xsl:choose>
                 <xsl:when test="year = -1">
                    <BR/>
                 </xsl:when>
                 <xsl:otherwise>
                    <xsl:value-of select="year"/>
                 </xsl:otherwise>
              </xsl:choose>
          </td>
          <td width="10%"><xsl:value-of select="covertype"/></td>
          <td width="15%">
              <xsl:choose>
                 <xsl:when test="isbn">
                    <xsl:value-of select="isbn"/>
                 </xsl:when>
                 <xsl:otherwise>
                    <BR/>
                 </xsl:otherwise>
              </xsl:choose>
          </td>
       </tr>
    </xsl:for-each>
    </table>
    </BODY>
    </HTML>
  </xsl:template>

</xsl:stylesheet>
