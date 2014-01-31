<!DOCTYPE html>
<html lang="en">
<head>
    <title>Users Profiles</title>
    <%@ include file="inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="inclusions/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-users"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
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

<jsp:include page="inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="scripts/presenters/UsersProfilesPresenter.js"/>
</jsp:include>

</body>
</html>