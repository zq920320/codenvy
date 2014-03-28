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
    <title>Sessions</title>
    <%@ include file="/inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="/inclusions/top-menu/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-sessions"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="filter-by" class="left" targetWidgets="_all">
                    Filter by:
                    <input type="text" id="filterByKeywordInput" name="keyword" class="text-box" />
                    <button class="btn command-btn" value="user">User</button>
                    <button class="btn command-btn" value="ws">Workspace</button>                    
                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                </div>
                
                <div id="date-range" class="right" targetWidgets="_all">
                    <span>
                        From: <input type="text" id="datepicker-from-date" name="from_date" class="date-box"/>
                        To: <input type="text" id="datepicker-to-date" name="to_date" class="date-box"/>
                    </span>
                    <button class="btn command-btn">Filter</button>
                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                </div>
            </div>
            
            <div class="hero-unit">
                <div class="single-column-gadget">
                    <div class="view">
                        <div class="tables">
                            <div class="item" id="sessions"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/EntryViewPresenter.js"/>
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