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
    <title>Factory Report</title>
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
    <jsp:param name="selectedMenuItemId" value="topmenu-reports-factory_report"/>
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
                               <td><label for="input-factory">Factory URL:</label></td>
                               <td><div class="filter-item">
                                   <input type="text" id="input-factory" name="factory" class="text-box" />
                               </div></td>
                            </tr>
                            <tr>
                                <td><label for="input-factory">Factory ID:</label></td>
                                <td><div class="filter-item">
                                    <input type="text" id="input-factory" name="factory_id" class="text-box" />
                                </div></td>
                            </tr>
                            <tr>
                               <td><label for="input-aliases">User:</label></td>
                               <td><div class="filter-item">
                                   <input type="text" id="input-aliases" name="aliases" class="text-box" />
                               </div></td>
                            </tr>
                            <tr>
                               <td><label for="input-org_id">Organization:</label></td>
                               <td><div class="filter-item">
                                   <input type="text" id="input-org_id" name="org_id" class="text-box" />
                               </div></td>
                            </tr>
                            <tr>
                               <td><label for="input-affiliate_id">Affiliate:</label></td>
                               <td><div class="filter-item">
                                   <input type="text" id="input-affiliate_id" name="affiliate_id" class="text-box" />
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
                                <td><div  class="filter-item">
                                    <button class="btn command-btn btn-primary">Filter</button>
                                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                                </div></td>
                            </tr>
                        </table>
                    </div>
                </div>

                <div id="ui-preferences" class="btn-group preferences right">
                    <button class="btn command-btn" default value="table">Table</button>
                    <button class="btn command-btn" value="chart">Chart</button>
                    <button class="btn command-btn" value="table&chart">Table & Chart</button>
                </div>

                <div id="timely-dd" class="btn-group right" targetWidgets="_all">
                    <button class="btn command-btn" default value="Day">Day</button>
                    <button class="btn command-btn" value="Week">Week</button>
                    <button class="btn command-btn" value="Month">Month</button>
                    <button class="btn command-btn" value="LifeTime">LifeTime</button>
                </div>
            </div>

            <div class="hero-unit">
                <div class="single-column-gadget">
                    <div class="view">
                        <div class="tables">
                            <div id="factoryReport"></div>
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
