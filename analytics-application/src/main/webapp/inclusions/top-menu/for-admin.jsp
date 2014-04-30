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
				<a class="nav" href="/analytics/pages/users-view.jsp?sort=%2Buser" id="topmenu-users">Users</a>
                <a class="nav" href="/analytics/pages/sessions-view.jsp?sort=-date" id="topmenu-sessions">Sessions</a>
				<a class="nav" href="/analytics/pages/workspaces-view.jsp?sort=%2Bws" id="topmenu-workspaces">Workspaces</a>
				<a class="nav" href="/analytics/pages/factories-view.jsp?sort=%2Bws_created" id="topmenu-factories">Factories</a>
                <a class="nav" href="/analytics/pages/projects-view.jsp" id="topmenu-projects">Projects</a>
                <a class="nav" href="/analytics/pages/events-view.jsp?sort=-date" id="topmenu-events">Events</a>

				<div class="nav">
					<div>
						<button id="topmenu-reports">Reports</button>
					</div>
					<ul class="dropdown-menu">
					    <li><a href="/analytics/pages/reports/summary-report.jsp" id="topmenu-reports-summary_report">Summary</a></li>
                        <li><a href="/analytics/pages/reports/workspace-report.jsp" id="topmenu-reports-workspace_report">Workspace</a></li>
                        <li><a href="/analytics/pages/reports/user-report.jsp" id="topmenu-reports-user_report">User</a></li>
                        <li><a href="/analytics/pages/reports/collaboration-report.jsp" id="topmenu-reports-collaboration_report">Collaboration</a></li>
                        <li><a href="/analytics/pages/reports/usage-report.jsp" id="topmenu-reports-usage_report">Usage</a></li>
                        <li><a href="/analytics/pages/reports/session-report.jsp" id="topmenu-reports-session_report">Session</a></li>
                        <li><a href="/analytics/pages/reports/project-report.jsp" id="topmenu-reports-project_report">Project</a></li>
					    <li><a href="/analytics/pages/reports/factory-statistics.jsp" id="topmenu-reports-factories">Factory</a></li>
					    <li><a href="/analytics/pages/reports/top-metrics.jsp" id="topmenu-reports-top_metrics">Top Metrics</a></li>
					    <li><a href="/analytics/pages/reports/signup-analysis.jsp" id="topmenu-reports-analysis">Analysis</a></li>
				    </ul>
				</div>
		    </div>

			<div class="right">
                <div class="nav">
                    <div>
                        <button id="topmenu-preferences">Preferences</button>
                    </div>
                    <ul class="dropdown-menu">
                        <li><div class="menu-group-title">IDE Version:</div>
		                    <ul id="ide-version" targetWidgets="_all">
		                        <li><a class="command-btn" default>IDE<i>2</i> & IDE<i>3</i></a></li>
		                        <li><a class="command-btn" value="2">IDE<i>2</i> only</a></li>
		                        <li><a class="command-btn" value="3">IDE<i>3</i> only</a></li>
			                </ul>
			            </li>
	                    <li><div class="menu-group-title">Data Universe:</div>
		                    <ul id="data-universe" targetWidgets="_all">
                                <li><a class="command-btn" default>Any workspaces</a></li>
                                <li><a class="command-btn" value="workspace/developer">Any workspace where the current user has workspace/developer right</a></li>
                                <li><a class="command-btn" value="workspace/admin">Any workspace where the current user has workspace/admin right</a></li>
                                <li><a class="command-btn" value="account/member">Any organization this user has account/member rights to</a></li>
                                <li><a class="command-btn" value="account/owner">Any organization this user has account/owner rights to</a></li>
		                    </ul>	                    
	                    </li>
                    </ul>
                </div>

				<div class="nav">
					<div class="label-container">
					    <div class="label"><%= FrontEndUtil.getFirstAndLastName(request.getUserPrincipal())%></div>
					</div>
					<button id="topmenu-user">&nbsp;</button>
				</div>
                <ul class="dropdown-menu">
					<li><a href="/analytics/pages/accounts-view.jsp">Organizations</a></li>
					<li><a href="/">Main page</a></li>
					<li><a href="/site/private/select-tenant">Workspace</a></li>
					<li><a href="#" onClick = "analytics.util.processUserLogOut()">Logout</a></li>
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

	    analytics.views.topMenu.turnOnDropdownButton("topmenu-preferences", false);    // turn-on preferences menu button

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