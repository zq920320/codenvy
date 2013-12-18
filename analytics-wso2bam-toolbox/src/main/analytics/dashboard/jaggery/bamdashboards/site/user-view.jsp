<% if (session.getAttribute("carboncookie") == null) { %>
<script type="text/javascript">
    location.href = "../index.jag";
</script>
<% }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Analytics</title>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-responsive.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/bam-dashboard-common-styles.css"/>
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }

        .sidebar-nav {
            padding: 9px 0;
        }
    </style>
</head>
<body>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <a class="brand" href="#"><img src="images/codenvy-logo.png" alt=""><span style="color: #B4D8FF;margin: 0 10px;"></span></a>
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span2">
            <div class="well sidebar-nav">
                <ul id="leftnav" class="nav nav-list"/>
            </div>
        </div>
        <div class="span10">
            <div class="hero-unit full-width">
                <div id="dashboardWidget-overview" src="gadgets/user-overview.jag"></div>
                <div class="well topFilteringPanel">
                   <!--  div id="filter-by" class="btn-group" target="dashboardWidget-statistics" -->
	 	             <div id="date-range" class="btn-group" target="dashboardWidget-statistics">
	                    <span>
	                        From: <input type="text" id="datepicker-from-date" name="from_date" class="date-box" />
	                        To: <input type="text" id="datepicker-to-date" name="to_date" class="date-box" />
	                    </span>
	                    <button class="btn">Filter</button>
	                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>  
                   </div>
                </div>
	             <div id="dashboardWidget-statistics" class="single-column-gadget full-width" src="gadgets/user-statistics.jag"></div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript" src="scripts/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>
<script type="text/javascript" src="scripts/bam.dashboard.main.js"></script>
<script type="text/javascript" src="../navigation.populator.js"></script>

<!--  load calendar jquery plugin  -->
<link rel="stylesheet" href="scripts/jquery-ui-1.8.20/themes/base/minified/jquery-ui.min.css">
<script type="text/javascript" src="scripts/jquery-ui-1.8.20/ui/minified/jquery-ui.min.js"></script>
<script type="text/javascript">
   $(function() {
      $("#datepicker-from-date").datepicker({dateFormat: "yy-mm-dd"});
      $("#datepicker-to-date").datepicker({dateFormat: "yy-mm-dd"});
   });
</script>

<!--  dasboard widgets  -->
<script>
   loadDashboardWidget("gadgets/user-overview.jag", "dashboardWidget-overview");  
   loadDashboardWidget("gadgets/user-statistics.jag", "dashboardWidget-statistics");
</script>

</body>
</html>
