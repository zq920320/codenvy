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
<%@page import="com.codenvy.analytics.util.FrontEndUtil" %>  

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container-fluid" id="topmenu">
			<a class="brand" href="/analytics/">
			     Codenvy <span class="analytics-label">Analytics</span>
			</a>

            <div class="left">
				<a class="nav" href="/analytics/pages/user-view.jsp?user=<%=request.getUserPrincipal().getName() %>" id="topmenu-users">User</a>
                <a class="nav" href="#">Workspaces</a> 
                <a class="nav" href="#">Projects</a> 
                <a class="nav" href="#">Factories</a>
		    </div>

			<div class="right">
				<div class="nav">
					<div class="label-container">
					    <div class="label"><%= FrontEndUtil.getFirstAndLastName(request.getUserPrincipal())%></div>
					</div>
					<button id="topmenu-user">&nbsp;</button>
				</div>
                <ul class="dropdown-menu">
					<li><a href="#" onClick = "analytics.util.processUserLogOut()">Logout</a></li>
					<li><a href="/">Main page</a></li>
					<li><a href="/site/private/select-tenant">Workspace</a></li>
				</ul>
            </div>
		</div>
	</div>
</div>

<!-- add handlers of top-menu buttons -->
<script type="text/javascript" src="/analytics/scripts/views/top-menu.js"></script>
<script>
	$(function() {
	    analytics.views.topMenu.turnOnNavButtons();
	    analytics.views.topMenu.turnOnDropdownButton("topmenu-reports", false);    // turn-on reports menu button
	    analytics.views.topMenu.turnOnDropdownButton("topmenu-user", true);    // turn-on user menu button
	    
	    
	    // select menu items connected to page where top menu is displaying
	<%  if (request.getParameterValues("selectedMenuItemId") != null) { 
	        String[] menuItemIds = request.getParameterValues("selectedMenuItemId");
	        for (int i = 0; i < menuItemIds.length; i++) { 
	%>
	    analytics.views.topMenu.selectMenuItem("<%= menuItemIds[i]%>");  
	<%      }
	    }
	%>

        analytics.views.topMenu.addHandlersToHidePopupMenu();
	});
</script>