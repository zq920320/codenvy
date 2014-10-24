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
    <title>Factories</title>
    <%@ include file="/inclusions/header.jsp" %>
</head>
<body>

<jsp:include page="/inclusions/top-menu/top-menu.jsp">
    <jsp:param name="selectedMenuItemId" value="topmenu-factories"/>
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
                               <td><label for="input-user">User:</label></td>
                               <td><div class="filter-item">
                                   <input type="text" id="input-user" name="user" class="text-box" />
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
                            <tr>
                               <td></td>
                               <td><div class="filter-item">
                                    <button class="btn command-btn btn-primary">Filter</button>                    
                                    <button id="clearSelectionBtn" class="btn btn-small clear-btn">Clear</button>
                               </div></td>
                            </tr>
                        </table>
                    </div>
                </div>
                
                <div id="show-factories" class="btn-group right" targetWidgets="_all">
                    <button class="btn command-btn" default value="">All Factories</button>
                    <button class="btn command-btn" value="1">Encoded Factories</button>
                    <button class="btn command-btn" value="0">Non Encoded Factories</button>
                </div>
            </div>
            
            <div class="hero-unit">                
                <div class="single-column-gadget">
                    <div class="view">
                        <div class="tables">
                            <div class="item" id="factoriesOverview"></div>
                            <div class="item" id="factories"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/inclusions/footer.jsp">
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/SummaryTablePresenter.js"/>
    <jsp:param name="javaScriptToLoad" value="/analytics/scripts/presenters/EntryViewPresenter.js"/>
</jsp:include>

</body>
</html>
