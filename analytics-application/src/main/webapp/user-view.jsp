<!DOCTYPE html>
<html lang="en">
<head>
    <title>User View</title>
    <%@ include file="inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="inclusions/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-users"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="hero-unit full-width">
                <div class="well topFilteringPanel">
                    <div id="date-range" class="btn-group"
                         targetWidgets="userData,userSessions,userWorkspaceData,userActivity">
                       <span>
                           From: <input type="text" id="datepicker-from-date" name="from_date" class="date-box"/>
                           To: <input type="text" id="datepicker-to-date" name="to_date" class="date-box"/>
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

                            <div class="item">
                                <div class="header">User Action</div>
                                <div class="body" id="userEvents"></div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>

<jsp:include page="inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="scripts/presenters/VerticalTablePresenter.js"/>
    <jsp:param name="javaScriptToLoad" value="scripts/presenters/TablePresenter.js"/>
</jsp:include>

<!--  load calendar jquery plugin  -->
<script type="text/javascript">
    $(function () {
        $("#datepicker-from-date").datepicker({dateFormat: "yy-mm-dd"});
        $("#datepicker-to-date").datepicker({dateFormat: "yy-mm-dd"});
    });
</script>

</body>
</html>
