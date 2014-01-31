<%@page import="java.security.Principal,com.codenvy.analytics.datamodel.*,java.util.*,com.codenvy.analytics.metrics.MetricFactory" %>  
<%  
   /** get first name and last name from special metric **/

   String METRIC_NAME = "users_profiles_list";
   String USER_FIRST_NAME_KEY = "user_first_name";
   String USER_LAST_NAME_KEY = "user_last_name";

   String email = null;
   Principal userPrincipal = request.getUserPrincipal();
   if (userPrincipal != null) {
       email = userPrincipal.getName();
   }
   
   HashMap<String, String> metricContext = new HashMap<String, String>();
   
   if (email != null) {
       metricContext.put("USER", email);
   }    
   
   ListValueData value = (ListValueData) MetricFactory.getMetric(METRIC_NAME).getValue(metricContext);
   
   String firstName = "";
   String lastName = "";
   
   if (value.size() > 0) {
	   Map<String,ValueData> userProfile = ((MapValueData) value.getAll().get(0)).getAll();
	   
	   firstName = userProfile.get(USER_FIRST_NAME_KEY).toString();
	   lastName = userProfile.get(USER_LAST_NAME_KEY).toString();
   }

   // display user email if there are empty both his/her first name and last name
   if (firstName.isEmpty() && lastName.isEmpty()) {
       firstName = email;
   }
   
   request.setAttribute(USER_FIRST_NAME_KEY, firstName);   
   request.setAttribute(USER_LAST_NAME_KEY, lastName);
%>

<style>
	.ui-button, .ui-button a {
	    color: white !important;
	    font-weight: bold !important;
	    font-size: 16px !important;
	}
	
	.ui-state-default {
	    background: none;
	    border: 0 none !important;
	}
	
	.selected {
	    background-color: #0076B1;
	    color: white !important;
	}
	
	.ui-state-hover {
	    background-color: #08c;
	}
	
	.ui-corner-all {
	    border-radius: 0 !important;
	}
</style>

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container-fluid" id="topmenu">
			<a class="brand" href="/analytics/">
			     Codenvy Analytics
			</a>

            <div>
				<a class="nav" href="users-profiles.jsp" id="topmenu-users">Users</a>
				<a class="nav" href="#">Workspaces</a>
				<a class="nav" href="#">Sessions</a> 
				<a class="nav" href="#">Projects</a> 
				<a class="nav" href="#">Factories</a>
	
				<div class="nav">
					<div>
						<button id="topmenu-reports">Reports</button>
					</div>
					<ul class="dropdown-menu">
					    <li><a href="timeline.jsp" id="topmenu-reports-timeline">Timeline</a></li>
					    <li><a href="factory-statistics.jsp" id="topmenu-reports-factories">Factories</a></li>
					    <li><a href="top-metrics.jsp" id="topmenu-reports-top_metrics">Top Metrics</a></li>
					    <li><a href="analysis.jsp" id="topmenu-reports-analysis">Analysis</a></li>
				    </ul>	    
				</div>
		    </div>

			<div class="right">
				<div class="nav">
					<div class="label"><%= request.getAttribute(USER_FIRST_NAME_KEY)%> <%= request.getAttribute(USER_LAST_NAME_KEY)%></div>
					<button id="topmenu-user">Select an action</button>
				</div>
                <ul class="dropdown-menu">
					<li><a href="/api/auth/logout">Logout</a></li>
					<li><a href="/">Main page</a></li>
					<li><a href="/site/private/select-tenant/">Workspace</a></li>
				</ul>
            </div>
		</div>
	</div>
</div>

<!-- add handlers of top-menu buttons -->
<script type="text/javascript" src="scripts/views/top-menu.js"></script>
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