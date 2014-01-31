<!DOCTYPE html>
<html lang="en">
<head>
    <title>Timeline</title>
    <%@ include file="inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="inclusions/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-reports"/>
    <jsp:param name="selectedMenuItemId" value="topmenu-reports-timeline"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="timely-dd" class="btn-group timely-dd-btns left" targetWidgets="timeline">
                    <button class="btn command-btn" default>Day</button>
                    <button class="btn command-btn">Week</button>
                    <button class="btn command-btn">Month</button>
                    <button class="btn command-btn">LifeTime</button>
                </div>
                <div id="filter-by" class="right" targetWidgets="timeline">
                     Filter by:
                    <input type="text" id="filterByKeywordInput" name="keyword" class="text-box" />
                    <button class="btn command-btn">Email</button>
                    <button class="btn command-btn">Domain</button>
                    <button class="btn command-btn">Company</button>
                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>                   
                </div>
            </div>
            <div class="hero-unit">
                <div id="timeline" class="single-column-gadget"></div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="scripts/presenters/ReportPresenter.js"/>
</jsp:include>

</body>
</html>