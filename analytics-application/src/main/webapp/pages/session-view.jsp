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
    <title>Session View</title>
    <%@ include file="/inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="/inclusions/top-menu/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-sessions"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="hero-unit">
                <div id="sessionOverview"></div>
                
	            <div class="well topFilteringPanel">
	                <div id="filter-by" class="left">
	                    <label><input type="checkbox" checked=true id="hide-session-events" 
                               value="~session-started,~session-finished,~session-factory-started,~session-factory-stopped" 
                               targetWidgets="userSessionActivity" />
	                       hide micro events
	                    </label>
	                </div>
	            </div>

                <div class="single-column-gadget full-width">
                    <div class="view">
                        <div class="tables">
                            <div class="item">
                                <div class="header">Session Events</div>
                                <div class="body" id="userSessionActivity"></div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/VerticalTablePresenter.js"/>
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/TablePresenter.js"/>
</jsp:include>

</body>
</html>
