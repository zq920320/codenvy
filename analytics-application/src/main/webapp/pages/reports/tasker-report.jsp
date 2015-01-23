<%--

    CODENVY CONFIDENTIAL
    __________________

     [2012] - [2014] Codenvy, S.A.
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
    <title>Tasker Report</title>
    <%@ include file="/inclusions/header.jsp"%>
    <style type="text/css">
        /* redefine style of Accordion Widget of jQuery UI (@see http://api.jqueryui.com/accordion/#method-disable ) */
        .ui-accordion {
            width: 540px;
        }
    </style>
</head>
<body>

<jsp:include page="/inclusions/top-menu/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-reports"/>
    <jsp:param name="selectedMenuItemId" value="topmenu-reports-tasker_report"/>
</jsp:include>

<div class="container-fluid">
    <div class="row-fluid">
        <div>
            <div class="well topFilteringPanel">
                <div id="filter-by" class="left" targetWidgets="_all">
                    <div class="collabsiblePanelTitle">Filter</div>
                    <div class="collabsiblePanelBody">
                        <table>
                            <tr>
                                <td><label for="input-ws">Workspace:</label></td>
                                <td><div class="filter-item">
                                    <input type="text" id="input-ws" name="ws" class="text-box" />
                                </div></td>
                            </tr>
                            <tr>
                                <td><label for="input-user">User:</label></td>
                                <td><div class="filter-item">
                                    <input type="text" id="input-user" name="user" class="text-box" />
                                </div></td>
                            </tr>
                        </table>
                        <table>
                            <tr>
                                <td><label for="datepicker-from-date">From Date:</label></td>
                                <td>
                                    <div class="filter-item">
                                        <input type="text" id="datepicker-from-date" name="from_date" class="short-date-box"/>
                                    </div>
                                </td>
                                <td class="short-gap" />
                                <td><label for="datepicker-to-date" class="right">To Date:</label></td>
                                <td>
                                    <div class="filter-item">
                                        <input type="text" id="datepicker-to-date" name="to_date" class="short-date-box"/>
                                    </div>
                                </td>
                                <td class="short-gap" />
                                <td><div>
                                    <button class="btn command-btn btn-primary">Filter</button>
                                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                                </div></td>
                            </tr>
                        </table>
                    </div>
                </div>

                <div id="timely-dd" class="btn-group right" targetWidgets="_all">
                    <button class="btn command-btn" value="Day">Day</button>
                    <button class="btn command-btn" value="Week">Week</button>
                    <button class="btn command-btn" default value="Month">Month</button>
                    <button class="btn command-btn" value="LifeTime">LifeTime</button>
                </div>
            </div>

            <div class="hero-unit">
                <div class="single-column-gadget">
                    <div class="view">
                        <div class="tables">
                            <div id="taskerReport"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/ReportPresenter.js"/>
</jsp:include>

</body>
</html>
