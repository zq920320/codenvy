<!DOCTYPE html>
<html lang="en">
<head>
    <title>Analysis</title>
    <%@ include file="inclusions/header.jsp"%>
</head>
<body>

<jsp:include page="inclusions/top-menu.jsp">
    <jsp:param name="selectedMenuItem" value="Reports"/>
    <jsp:param name="selectedSubMenuItem" value="Analysis"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span10">
            <div class="single-column-gadget full-width">
                <div class="view">
                    <div class="tables">
                        <div class="item">
                            <div class="header">Signup Analysis</div>
                            <div class="body" id="analysis"></div>
                        </div>
                        
                        <div class="item">
                            <div class="header">Condition</div>
                            <div class="body" id="timeline_product_usage_condition"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="scripts/presenters/ReportPresenter.js"/>
</jsp:include>

</body>
</html>
