<%--

    CODENVY CONFIDENTIAL
    __________________

     [2012] - [2014] Codenvy, S.A.
     All Rights Reserved.

    NOTICE:  All information contained herein is, and remains
    the property of Codenvy S.A. and its suppliers,
    if any.  The intellectual and technical concepts contained
    herein are proprietary to Codenvy S.A.
    and its suppliers and may be covered by U.S. and Foreign Patents,
    patents in process, and are protected by trade secret or copyright law.
    Dissemination of this information or reproduction of this material
    is strictly forbidden unless prior written permission is obtained
    from Codenvy S.A..

--%>
<%@page import="com.codenvy.analytics.util.FrontEndUtil" %>

<%@ include file="/inclusions/top-menu/header.jsp" %>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid" id="topmenu">
            <a class="brand" href="/analytics/">
                <img src="/analytics/images/logo2.png" alt="Codenvy | Header logo" height="20px">
            </a>

            <div class="left">
                <a class="nav" href="/analytics/pages/users-view.jsp" id="topmenu-users">Users</a>
                <a class="nav" href="/analytics/pages/sessions-view.jsp" id="topmenu-sessions">Sessions</a>
                <a class="nav" href="/analytics/pages/workspaces-view.jsp" id="topmenu-workspaces">Workspaces</a>
                <a class="nav" href="/analytics/pages/factories-view.jsp" id="topmenu-factories">Factories</a>
                <a class="nav" href="/analytics/pages/projects-view.jsp" id="topmenu-projects">Projects</a>
                <a class="nav" href="/analytics/pages/events-view.jsp" id="topmenu-events">Events</a>

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
                        <li><a href="/analytics/pages/reports/key-feature-usage-report.jsp" id="topmenu-reports-key_feature_usage_report">Key Feature Usage</a></li>
                        <li><a href="/analytics/pages/reports/session-report.jsp" id="topmenu-reports-session_report">Session</a></li>
                        <li><a href="/analytics/pages/reports/project-report.jsp" id="topmenu-reports-project_report">Project</a></li>
                        <li><a href="/analytics/pages/reports/factory-report.jsp" id="topmenu-reports-factory_report">Factory</a></li>
                        <li><a href="/analytics/pages/reports/runner-report.jsp" id="topmenu-reports-runner_report">Runner</a></li>
                        <li><a href="/analytics/pages/reports/builder-report.jsp" id="topmenu-reports-builder_report">Builder</a></li>
                        <li><a href="/analytics/pages/reports/top-factory-sessions.jsp" id="topmenu-reports-top_metrics">Top Metrics</a></li>
                        <li><a href="/analytics/pages/reports/signup-analysis.jsp" id="topmenu-reports-analysis">Analysis</a></li>
                        <li><a href="/analytics/pages/reports/time-tracking-workspaces.jsp" id="topmenu-reports-time_tracking">Time Tracking</a></li>
                    </ul>
                </div>
            </div>

            <div class="right">
               <div class="nav">
                   <div>             
                       <a href="#" onClick = "analytics.util.processUserLogOut()" class="logout"></a>
                   </div>
               </div>
            </div>
           
            <div class="right">
                <div class="nav">                    
                    <div>
                        <button id="topmenu-user"><%= FrontEndUtil.getFirstAndLastName(request.getUserPrincipal())%></button>
                    </div>
                    <ul class="dropdown-menu">
                        <li><a href="/analytics/pages/user-view.jsp?user=<%= FrontEndUtil.getCurrentUserId(request)%>">My statistics</a></li>
                        <li><a href="/site/private/select-tenant">My workspace</a></li>
                        <% if (request.isUserInRole("user")) { %>
                            <li><a href="/analytics/pages/accounts-view.jsp">Organization</a></li>
                        <% } %>
                        <li><a href="/">Codenvy main page</a></li>
                        <li><a href="#" onclick="analytics.util.processUserLogOut()">Logout</a></li>
                    </ul>                   
               </div>
            </div>
            
            <div class="right">
                <div class="nav">
                    <div>
                        <button id="topmenu-preferences">Data Universe</button>
                    </div>
                    <ul class="dropdown-menu">
                        <li>
                            <ul id="data-universe" targetWidgets="_all">
                                <li><a class="command-btn" default>Display statistics by Any workspaces</a></li>
                                <li><a class="command-btn" value="workspace/developer">Display statistics by workspaces where I have workspace/developer right</a></li>
                                <li><a class="command-btn" value="workspace/admin">Display statistics by workspaces where I have workspace/admin right</a></li>
                                <li><a class="command-btn" value="account/member">Display statistics by workspaces where I have account/member rights to</a></li>
                                <li><a class="command-btn" value="account/owner">Display statistics by workspaces where I have account/owner rights to</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>

                <img src="/analytics/images/user.png" height="24px" />
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
        analytics.views.topMenu.turnOnDropdownButton("topmenu-user", false);    // turn-on user menu button

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
