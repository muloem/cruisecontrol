<!--********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 500
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************-->
<%@page contentType="text/html"%>
<%@ taglib uri="/WEB-INF/cruisecontrol-jsp11.tld" prefix="cruisecontrol"%>
<html>
<head>
  <title>CruiseControl Build Results</title>
  <link type="text/css" rel="stylesheet" href="css/cruisecontrol.css"/>
</head>
<body background="images/bluebg.gif" topmargin="0" leftmargin="0" marginheight="0" marginwidth="0">
  <table border="0" align="center" cellpadding="0" cellspacing="0" width="98%">
    <tr>
      <td rowspan="5" valign="top">
        <img src="images/blank8.gif" border="0"><br>
        <a href="http://cruisecontrol.sourceforge.net" border="0"><img src="images/logo.gif" border="0"></a><p>
        <table border="0" align="center" width="98%">
            <tr><td><cruisecontrol:currentbuildstatus/></td></tr>
            <tr><td>&nbsp;</td></tr>
            <cruisecontrol:nav startingBuildNumber="0" finalBuildNumber="10" >
                <tr><td><a class="link" href="<%= url %>"><%= linktext %></a></td></tr>
            </cruisecontrol:nav>
            <tr><td>
              <form method="GET" action="<%=request.getContextPath() + request.getServletPath()%>" >
                <select name="log" onchange="form.submit()">
                  <cruisecontrol:nav startingBuildNumber="10">
                    <option value="<%=logfile%>"><%= linktext %></option>
                  </cruisecontrol:nav>
                </select>
              </form>
            </td></tr>
        </table>
      </td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <% String queryString = (request.getQueryString() != null) ? request.getQueryString() : ""; %>
      <td><img src="images/blank35.gif"><a href="/cruisecontrol/buildresults?<%= queryString %>" border="0"><img src="images/buildResultsTab-off.gif" border="0"></a><img src="images/testResultsTab-on.gif" border="0"><a href="/cruisecontrol/xmllog?<%= queryString %>" border="0"><img src="images/xmlLogFileTab-off.gif" border="0"></a><a href="/cruisecontrol/controlpanel?<%= queryString %>" border="0"><img src="images/controlPanelTab-off.gif" border="0"></a></td>
    </tr>
    <tr>
      <td background="images/bluestripestop.gif"><img src="images/blank8.gif" border="0"></td>
    </tr>
    <tr>
      <td valign="top" bgcolor="#FFFFFF">
         Test Details
         &nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>&nbsp;<p>
      </td>
    </tr>
    <tr>
      <td background="images/bluestripesbottom.gif"><img src="images/blank8.gif" border="0"></td>
    </tr>
  </table>
</body>
</html>