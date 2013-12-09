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
            <div class="hero-unit">
                <div id="dashboardWidget" class="single-column-gadget" src="gadgets/user-view.jag"></div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript" src="scripts/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>
<script type="text/javascript" src="scripts/bam.dashboard.main.js"></script>
<script type="text/javascript" src="../navigation.populator.js"></script>

<script>
   (function() {
	   var url = "gadgets/user-view.jag";
	   
	   var userId = "<%=request.getParameter("userid")%>";
	   if (userId != "null") {
	      url += "?userid=" + encodeURIComponent(userId);
	   }
	   
	   jQuery("#dashboardWidget").load(url);
   })()
</script>

</body>
</html>
