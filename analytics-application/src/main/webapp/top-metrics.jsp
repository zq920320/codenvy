<!DOCTYPE html>
<html lang="en">
<head>
    <title>Top Metrics</title>
    <%@ include file="inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="inclusions/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-reports"/>
    <jsp:param name="selectedMenuItemId" value="topmenu-reports-top_metrics"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="metric" class="btn-group metric-btns left" targetWidgets="topMetrics">
                    <button class="btn command-btn" default>Top Factory Sessions</button>
                    <button class="btn command-btn">Top Factories</button>
                    <button class="btn command-btn">Top Referrers</button>
                    <button class="btn command-btn">Top Users</button>
                    <button class="btn command-btn">Top Domains</button>
                    <button class="btn command-btn">Top Companies</button>
                </div>            
                <div id="timely-dd" class="btn-group timely-dd-btns right" targetWidgets="topMetrics">
                    <button class="btn command-btn" default>1 Day</button>
                    <button class="btn command-btn">7 Days</button>
                    <button class="btn command-btn">30 Days</button>
                    <button class="btn command-btn">60 Days</button>
                    <button class="btn command-btn">90 Days</button>
                    <button class="btn command-btn">1 Year</button>
                    <button class="btn command-btn">LifeTime</button>
                </div>
            </div>
            <div class="hero-unit">
                <div id="topMetrics" class="single-column-gadget"></div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="scripts/presenters/TopMetricsPresenter.js"/>
</jsp:include>

</body>
</html>