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

.selected, .ui-state-hover {
    background-color: #0076B1;
    color: white !important;
}

.ui-corner-all {
    border-radius: 0 !important;
}
</style>
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container-fluid">
			<a class="brand" href="#">
			     Codenvy Analytics
			</a>

            <div>
				<button class="nav"><a href="users-profiles.jsp">Users</a></button>
				<button class="nav"><a href="#">Workspaces</a></button>
				<button class="nav"><a href="#">Sessions</a></button> 
				<button class="nav"><a href="#">Projects</a></button> 
				<button class="nav"><a href="#">Factories</a></button>
	
				<div class="nav">
					<div>
						<button id="reportMenuButton" class="selected">Reports</button>
					</div>
					<ul class="dropdown-menu">
					    <li><a href="timeline.jsp">Timeline</a></li>
					    <li><a href="factory-statistics.jsp">Factory statistics</a></li>
					    <li><a href="top-metrics.jsp">Top Metrics</a></li>
					    <li><a href="analysis.jsp" class="selected">Analysis</a></li>
				    </ul>	    
				</div>
		    </div>

			<div class="right">
				<div class="nav">
					<div class="brand">First_Name Last_Name</div>
					<button id="userMenuButton">Select an action</button>
				</div>
                <ul class="dropdown-menu">
					<li><a href="#">Logout</a></li>
					<li><a href="#">Main page</a></li>
					<li><a href="#">Workspace</a></li>
				</ul>
            </div>
		</div>
	</div>
</div>

<!-- add handlers of top-menu buttons -->
<script>
	$(function() {
	    turnOnNavButtons();
	    dropdownButtonAction("reportMenuButton", false);    // reports menu button
	    dropdownButtonAction("userMenuButton", true);    // user menu button
	});
	
	function turnOnNavButtons() {
	    var buttons = jQuery( "button.nav" );
	    for (var i = 0; i < buttons.length; i++)
	        jQuery(buttons[i]).button();
	}
	
	function dropdownButtonAction(selectButtonId, displayTriangleIcon) {
	    var buttonText = {};
	    if (displayTriangleIcon) {
	        buttonText = {
	            text: false,
	            icons: {
	                primary: "ui-icon-triangle-1-s"
	            }
	        };
	    }
	    
	    jQuery( "#" + selectButtonId )
	    .button(buttonText)
	    .click(function() {
	      var menu = $( this ).parent().next().show().position({
	        my: "left top",
	        at: "left bottom",
	        of: this
	      });
	      $( document ).one( "click", function() {
	        menu.hide();
	      });
	      return false;
	    });
	
	}
</script>