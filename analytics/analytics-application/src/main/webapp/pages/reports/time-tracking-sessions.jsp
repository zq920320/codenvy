<%--

    CODENVY CONFIDENTIAL
    __________________

     [2012] - [2015] Codenvy, S.A.
     All Rights Reserved.

    NOTICE:  All information contained herein is, and remains
    the property of Codenvy S.A. and its suppliers,
    if any.  The intellectual and technical concepts contained
    herein are proprietary to Codenvy S.A.
    and its suppliers and may be covered by U.S. and Foreign Patents,
    patents in process, and are protected by trade secret or copyright law.
    Dissemination of this information or reproduction of this material
    is strictly forbidden unless prior written permission is obtained
    from Codenvy S.A..

--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Time tracking by sessions</title>
    <%@ include file="/inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="/inclusions/top-menu/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-reports"/>
    <jsp:param name="selectedMenuItemId" value="topmenu-reports-time_tracking"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="metric" class="btn-group metric-btns left" targetWidgets="_all">
                    <button class="btn command-btn" value="time-tracking-workspaces.jsp">Workspaces</button>
                    <button class="btn command-btn" value="time-tracking-users.jsp">Users</button>
                    <button class="btn command-btn" default value="time-tracking-sessions.jsp">Sessions</button>
                </div>
                <div id="passed-days-count" class="btn-group right" targetWidgets="_all">
                    <button class="btn command-btn" default value="by_1_day">1 Day</button>
                    <button class="btn command-btn" value="by_7_days">7 Days</button>
                    <button class="btn command-btn" value="by_30_days">30 Days</button>
                    <button class="btn command-btn" value="by_lifetime">LifeTime</button>
                </div>
            </div>
            <div class="hero-unit">
                <div class="single-column-gadget">
                    <div class="view">
                        <div class="tables">
                            <div id="timeTrackingSessions"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/TopMetricsPresenter.js"/>
</jsp:include>

</body>
</html>