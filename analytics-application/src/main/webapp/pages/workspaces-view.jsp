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
    <title>Workspaces</title>
    <%@ include file="/inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="/inclusions/top-menu/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-workspaces"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="filter-by" class="left" targetWidgets="_all">
                    Filter by:
                    <input type="text" id="filterByKeywordInput" name="keyword" class="text-box" />
                    <button class="btn command-btn" value="ws">Workspace</button>
                    <button class="btn command-btn" value="session_id">Session</button>
                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                </div>
            </div>
            <div class="hero-unit">
                <div id="workspaces" class="single-column-gadget"></div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/EntryViewPresenter.js"/>
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/WorkspacesPresenter.js"/>
</jsp:include>

</body>
</html>