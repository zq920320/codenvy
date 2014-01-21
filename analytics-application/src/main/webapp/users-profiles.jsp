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
                   <li><a href="analysis.jsp">Analysis</a></li>
               </ul>
           </div>
        </div>
        <div class="span10">
            <div class="well topFilteringPanel">
                <div id="filter-by" class="left" targetWidgets="usersProfiles">
                    Filter by:
                    <input type="text" id="filterByKeywordInput" name="keyword" class="text-box" />
                    <button class="btn command-btn">Email</button>
                    <button class="btn command-btn">First Name</button>
                    <button class="btn command-btn">Last Name</button>                
                    <button class="btn command-btn">Company</button>
                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                </div>
            </div>
            <div class="hero-unit">
                <div id="usersProfiles" class="single-column-gadget"></div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript" src="scripts/third-party/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>

<!-- Prototypes -->
<script type="text/javascript" src="scripts/Presenter.js"></script>
<script type="text/javascript" src="scripts/presenters/UsersProfilesPresenter.js"></script>

<!-- Singletons -->
<script type="text/javascript" src="scripts/main.js"></script>

<script type="text/javascript" src="scripts/util.js"></script>
<script type="text/javascript" src="scripts/view.js"></script>
<script type="text/javascript" src="scripts/views/loader.js"></script>

<script type="text/javascript" src="scripts/model.js"></script>

<script type="text/javascript" src="scripts/factory.js"></script>
<script type="text/javascript" src="scripts/configuration.js"></script>
</body>
</html>
