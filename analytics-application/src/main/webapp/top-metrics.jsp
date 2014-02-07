<%-- 
 CODENVY CONFIDENTIAL
 ________________

 [2012] - [2014] Codenvy, S.A.
 All Rights Reserved.
 NOTICE: All information contained herein is, and remains
 the property of Codenvy S.A. and its suppliers,
 if any. The intellectual and technical concepts contained
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
                    <button class="btn command-btn" default value="top_factory_sessions">Top Factory Sessions</button>
                    <button class="btn command-btn" value="top_factories">Top Factories</button>
                    <button class="btn command-btn" value="top_referrers">Top Referrers</button>
                    <button class="btn command-btn" value="top_users">Top Users</button>
                    <button class="btn command-btn" value="top_domains">Top Domains</button>
                    <button class="btn command-btn" value="top_companies">Top Companies</button>
                </div>            
                <div id="timely-dd" class="btn-group timely-dd-btns right" targetWidgets="topMetrics">
                    <button class="btn command-btn" default value="1day">1 Day</button>
                    <button class="btn command-btn" value="7day">7 Days</button>
                    <button class="btn command-btn" value="30day">30 Days</button>
                    <button class="btn command-btn" value="60day">60 Days</button>
                    <button class="btn command-btn" value="90day">90 Days</button>
                    <button class="btn command-btn" value="365day">1 Year</button>
                    <button class="btn command-btn" value="lifetime">LifeTime</button>
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