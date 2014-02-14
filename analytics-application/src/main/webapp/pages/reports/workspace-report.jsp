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
    <title>Workspace Report</title>
    <%@ include file="/inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="/inclusions/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-reports"/>
    <jsp:param name="selectedMenuItemId" value="topmenu-reports-workspace_report"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="timely-dd" class="btn-group timely-dd-btns left" targetWidgets="_all">
                    <button class="btn command-btn" value="Day">Day</button>
                    <button class="btn command-btn" default value="Week">Week</button>
                    <button class="btn command-btn" value="Month">Month</button>
                    <button class="btn command-btn" value="LifeTime">LifeTime</button>
                </div>
                <div id="filter-by" class="right" targetWidgets="_all">
                     Filter by:
                    <input type="text" id="filterByKeywordInput" name="keyword" class="text-box" />
                    <button class="btn command-btn" value="user">Email</button>
                    <button class="btn command-btn" value="domain">Domain</button>
                    <button class="btn command-btn" value="user_company">Company</button>
                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>                   
                </div>
            </div>
            <div class="hero-unit">
                <div id="workspaceReport" class="single-column-gadget"></div>
            </div>            
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/ReportPresenter.js"/>
</jsp:include>

</body>
</html>