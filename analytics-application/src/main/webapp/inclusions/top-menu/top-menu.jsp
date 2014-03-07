<%-- 
 CODENVY CONFIDENTIAL
 ________________

 [2012] - [2014] Codenvy, S.A.
 All Rights Reserved.
 NOTICE: All information contained herein is, and remains
 the property of Codenvy S.A. and its suppliers,
 if any. The intellectual and technical concepts contained
 herein are proprietary to Codenvy S.A.
 and its suppliers and may be covered by U.S. and Foreign Patents,
 patents in process, and are protected by trade secret or copyright law.
 Dissemination of this information or reproduction of this material
 is strictly forbidden unless prior written permission is obtained
 from Codenvy S.A.. 
--%>
<%@ page import="com.codenvy.analytics.util.Utils" %>

<%@ include file="/inclusions/top-menu/header.jsp"%>

<% if (Utils.isSystemUser(request.getUserPrincipal().getName())) { %>
	<jsp:include page="/inclusions/top-menu/for-admin.jsp">
	    <jsp:param name="selectedMenuItemId" value="<%=request.getParameter("selectedMenuItemId") %>"/>
	</jsp:include>
<% } else { %>
	<jsp:include page="/inclusions/top-menu/for-user.jsp">
	    <jsp:param name="selectedMenuItemId" value="<%=request.getParameter("selectedMenuItemId") %>"/>
	</jsp:include>
<% } %>
