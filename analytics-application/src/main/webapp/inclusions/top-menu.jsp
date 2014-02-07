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

<style>
	.ui-button, .ui-button a {
	    color: white !important;
	    font-weight: bold !important;
	    font-size: 16px !important;
	    margin-right: 0px;
	}
	
	.ui-state-default {
	    background: none;
	    border: 0 none !important;
	}
	
	.selected {
	    background-color: #0076B1;
	}
	
	.ui-state-hover {
	    background-color: #08c;
	}
	
	.ui-corner-all {
	    border-radius: 0 !important;
	}
	
	.analytics-label {
        color: #0076B1;
    }

    /* button set */    
    .button-set .ui-state-hover {
        background-color: red;
    }
    
    .button-set .selected {
        background-color: purple;
    }

    .button-set .ui-button {
        font-size: 14px !important;
    }
    
    
    /* use white "ui-icon-triangle-1-s" icon in user menu button */
    .ui-state-hover .ui-icon, .ui-state-focus .ui-icon,
    .ui-state-default .ui-icon {
        background-image: url(images/ui-icons_ffffff_256x240.png); 
    }
</style>

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container-fluid" id="topmenu">
			<a class="brand" href="/analytics/">
			     Codenvy <span class="analytics-label">Analytics</span>
			</a>

            <div class="left">
				<a class="nav" href="users-profiles.jsp" id="topmenu-users">Users</a>
				<a class="nav" href="#">Workspaces</a> 
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
				
				<!-- ><div id="ide-selector" class="nav button-set">
                    <button class="nav selected" id="topmenu-ide2&ide3" name="ide" value="">IDE<i>2</i> & IDE<i>3</i></button>
                    <button class="nav" id="topmenu-ide2" name="ide" value="~ide3">IDE<i>2</i></button>                    
                    <button class="nav" id="topmenu-ide3" name="ide" value="~ide2">IDE<i>3</i></button>
                </div-->
		    </div>


            <!-- div class="button-set">
                <div class="nav">
                    <label for="topmenu-ide2&ide3">IDE-2 & IDE-3</label>
                    <input type="radio" id="topmenu-ide2&ide3" checked="checked" name="ide" value="" />
                </div>
                <div class="nav">
                    <label for="topmenu-ide2">IDE-2</label>
                    <input type="radio" id="topmenu-ide2" name="ide" value="~ide3" />
                </div>
                <div class="nav">
                    <label for="topmenu-ide3">IDE-3</label>
                    <input type="radio" id="topmenu-ide3" name="ide" value="~ide2" />
                </div>
            </div-->

			<div class="right">			
                <div class="nav">                
                    <div>
                        <button id="topmenu-options">{<i>Preferences</i>}</button>
                    </div>
                    <ul class="dropdown-menu" id="ide-version" targetWidgets="_all">
	                    <li><button id="topmenu-ide2&ide3" class="command-btn" default>IDE<i>2</i> & IDE<i>3</i></button></li>
	                    <li><button id="topmenu-ide2" class="command-btn" value="~3">IDE<i>2</i> only</button></li>                    
	                    <li><button id="topmenu-ide3" class="command-btn" value="~2">IDE<i>3</i> only</button></li>
                    </ul> 
                </div>


				<div class="nav">
					<div class="label-container">
					    <div class="label"><%= FrontEndUtil.getFirstAndLastName(request.getUserPrincipal())%></div>
					</div>
					<button id="topmenu-user">&nbsp;</button>
				</div>
                <ul class="dropdown-menu">
					<li><a href="/api/auth/logout">Logout</a></li>
					<li><a href="/">Main page</a></li>
					<li><a href="/site/private/select-tenant">Workspace</a></li>
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
	    
	    analytics.views.topMenu.turnOnDropdownButton("topmenu-options", false);    // turn-on user menu button
	    
	    
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
        
//        analytics.views.topMenu.turnOnButtonSet("ide-selector");
        
        // setup radio buttons
        // $(function() {
//            $( "input[name='ide']" ).buttonset().click(function() {
//                console.log(this.value);
//            });
//        });
	});
</script>