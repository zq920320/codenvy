<!DOCTYPE html>
<html lang="en">
<head>
    <title>Analytics</title>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-responsive.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/styles.css"/>
    <link href="css/single-column.css" rel="stylesheet" type="text/css" />
    <link href="css/view.css" rel="stylesheet" type="text/css" />    
    
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
               <ul id="leftnav" class="nav nav-list">
                   <li class="nav-header">Codenvy Analytics</li>
                   <li><a href="timeline.jsp">Timeline</a></li>
                   <li><a href="factory-statistics.jsp">Factory statistics</a></li>
                   <li><a href="users-profiles.jsp">User's statistics</a></li>
                   <li><a href="top-metrics.jsp">Top Metrics</a></li>
               </ul>
           </div>
        </div>
        <div class="span10">
            <div class="hero-unit full-width">
                <div class="well topFilteringPanel">
                   <div id="date-range" class="btn-group" targetWidgets="userData,userSessions,userWorkspaceData,userActivity">
                       <span>
                           From: <input type="text" id="datepicker-from-date" name="from_date" class="date-box" />
                           To: <input type="text" id="datepicker-to-date" name="to_date" class="date-box" />
                       </span>
                       <button class="btn command-btn">Filter</button>
                       <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>  
                   </div>
                </div>
                <div id="userOverview"></div>
                
				<div class="single-column-gadget full-width">
					<div class="view">
						<div class="tables">
							<div class="item">
								<div class="header">User Statistics</div>
								<div class="body" id="userData"></div>
							</div>
							
                            <div class="item">
                                <div class="header">Sessions</div>
                                <div class="body" id="userSessions"></div>
                            </div>
                            
                            <div class="item">
                                <div class="header">Workspaces</div>
                                <div class="body" id="userWorkspaceData"></div>
                            </div>
                            
                            <div class="item">
                                <div class="header">User Logs</div>
                                <div class="body" id="userActivity"></div>
                            </div>
						</div>
					</div>
				</div>

			</div>
        </div>
    </div>
</div>

<script type="text/javascript" src="scripts/third-party/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>

<!-- Prototypes -->
<script type="text/javascript" src="scripts/Presenter.js"></script>
<script type="text/javascript" src="scripts/presenters/VerticalTablePresenter.js"></script>
<script type="text/javascript" src="scripts/presenters/TablePresenter.js"></script>

<!-- Singletons -->
<script type="text/javascript" src="scripts/main.js"></script>

<script type="text/javascript" src="scripts/util.js"></script>
<script type="text/javascript" src="scripts/view.js"></script>

<script type="text/javascript" src="scripts/views/loader.js"></script>

<script type="text/javascript" src="scripts/model.js"></script>

<script type="text/javascript" src="scripts/factory.js"></script>
<script type="text/javascript" src="scripts/configuration.js"></script>


<!--  load calendar jquery plugin  -->
<link rel="stylesheet" href="scripts/third-party/jquery-ui-1.8.20/themes/base/minified/jquery-ui.min.css">
<script type="text/javascript" src="scripts/third-party/jquery-ui-1.8.20/ui/minified/jquery-ui.min.js"></script>
<script type="text/javascript">
   $(function() {
      $("#datepicker-from-date").datepicker({dateFormat: "yy-mm-dd"});
      $("#datepicker-to-date").datepicker({dateFormat: "yy-mm-dd"});
   });
</script>

</body>
</html>
