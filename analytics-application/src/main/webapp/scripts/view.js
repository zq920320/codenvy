/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.view = new View();

function View() {

    var viewHtml;

    var widget;
    
    var params;

    var ABORT_LOADING_MESSAGE = "<i>Loading has been aborted.</i>";
    var INTERRUPT_LOADING_MESSAGE = "<i>Loading has been interrupted.</i>";

    function printTable(table, isDisplaySpecificFirstCell, tableId) {
        print("<div class='table-container'>");
        
        if (typeof tableId == "undefined") {
            print('<table cellspacing="0" class="database-table" align="center">');
        } else {
            print('<table cellspacing="0" class="database-table" align="center" id="' + tableId + '">');
        }
        
        print('<thead aria-hidden="false">');
        print('<tr>');
    
        // print first cell of header
        if (table.columns.length > 0) {
            var value = table.columns[0] || "&nbsp;";   // add space to be able to display icons in the empty cell of first column of header 
            print('<th class="header">');
            print("<div>" + value + "</div>");
            print('</th>');
        }
    
        // print other cells of header    
        for (var i = 1; i < table.columns.length; i++) {
            print('<th class="header">');
            print("<div>" + table.columns[i] + "</div>");
            print('</th>');
        }
        print('</tr>');
        print('</thead>');
    
        // print table body
        print('<tbody>');
        for (var i = 0; i < table.rows.length; i++) {
            // print odd row
            print('<tr>');
    
            var firstCellClass = "cell";
            if (typeof isDisplaySpecificFirstCell != "undefined" && isDisplaySpecificFirstCell) {
                firstCellClass += " first-cell text-cursor";
            }
    
            // print first cell
            print('<td class="' + firstCellClass + '">');
            print(table.rows[i][0]);
            print('</td>');
    
            // print another cells
            for (var j = 1; j < table.columns.length; j++) {
                print('<td class="cell">');
                print(table.rows[i][j]);
                print('</td>');
            }
            print('</tr>');
        }
    
        print('<tfoot aria-hidden="true" style="display: none;"></tfoot>');
        print('</tbody>');
        print('</table>');
        print("</div>");
    };
    
    function printCsvButton(csvButtonLink) {
        var csvButtonLabel = "CSV";
        
        print("<div class='small-links-block'>");
        print("  <a href='" + csvButtonLink + "' target='_blank'>");
        print(csvButtonLabel);
        print("  </a>");
        print("</div>");
    }
    
    function printWidgetHeader(widgetLabel, csvButtonLink) {
        if (typeof csvButtonLink != "undefined") {        
            printCsvButton(csvButtonLink);
        }
        
        print("<div class='header'>");
        print(widgetLabel);
        print("</div>");
    }
    
    function printTableVerticalRow(table) {     
        print('<table cellspacing="0" class="database-table-vertical-row" align="center">');
        print('<tbody>');
    
        // print other cells name + cell
        for (var i = 0; i < table.columns.length; i++) {
            print("<tr>");
            print("<th class='cell-name'>");
            print(table.columns[i] + ":");
            print("</th>");
    
            if (typeof table.rows != "undefined"
                    && typeof table.rows[0] != "undefined"
                    && typeof table.rows[0][i] != "undefined") {
                print("<td class='cell'>");
                print(table.rows[0][i]);
                print("</td>");
                print("</tr>");
            }
        }
    
        print('</tbody>');
        print('</table>');
    }   
        
    /**
     * Load handlers of table events.
     * Default value of displaySorting is true.
     */
    function loadTableHandlers(displaySorting, sortingParams, tableId) {
        var sortingParams = sortingParams || {};
        if (typeof displaySorting == "undefined") {
            displaySorting = true;
        }
        
        print("<script>");
        print("  jQuery(function() { ");
        print("       analytics.views.databaseTable.setupHorizontalTableRowHandlers("
                + displaySorting + ", '"
                + JSON.stringify(sortingParams) + "', '"
                + tableId + "');");
        print("       analytics.views.databaseTable.setupVerticalTableRowHandlers();");
        print("  });");
        print("</script>");
    }
    
    /**
     * Prints page navigator, meets the requirements: 1 ... 4 5 6 /7/ 9 10 11 ... 100.
     * CurrentPageNumber is 1-based. 
     */
    function printBottomPageNavigator(pageCount, currentPageNumber, params, pageQueryParameter, widgetName) {
        if (typeof pageCount == "undefined" || pageCount <= 0) {
            return;
        }
    
        // remove old page number
        var params = analytics.util.clone(params);
        delete params[widgetName];  
        delete params.page;
        var queryString = "";
        if (!jQuery.isEmptyObject(params)) {
            queryString += "?" + analytics.util.constructUrlParams(params);
        }
        
        print('<link href="/analytics/css/page-navigator.css" rel="stylesheet" type="text/css" />');
        print("<div class='bottom-page-navigator'>");
    
        for (var i = 1; i < pageCount + 1; i++) {
            var href = getPageNavigationUrl(queryString, i, pageQueryParameter);
    
            var onClickHandler = "analytics.main.reloadWidgetByUrl(\"" + href + "\",\"" + widgetName + "\"); return false;";
            
            
            if (i == currentPageNumber) {
                print("<a class='page-link current' href='" + href + "' onclick='" + onClickHandler + "'>" + i + "</a>");
    
            } else if (i == 1) {
                print("<a class='page-link' href='" + href + "' onclick='" + onClickHandler + "'>" + i + "</a>");
                if (currentPageNumber > 4                   
                        && pageCount > 5) {   // don't display "..." if pageCount < (2 * 3)
                    print(' ... ')
                }
    
            } else if (i == pageCount) {
                if (currentPageNumber < pageCount - 4 
                        && pageCount > 5) {   // don't display "..." if pageCount < (2 * 3)
                    print(' ... ')
                }
                print("<a class='page-link' href='" + href + "' onclick='" + onClickHandler + "'>" + i + "</a>");
    
            } else if (i + 3 >= currentPageNumber && currentPageNumber >= i - 3) {
                print("<a class='page-link' href='" + href + "' onclick='" + onClickHandler + "'>" + i + "</a>");
            }
        }
        print("</div>");
    }
    
    function getWidgetId() {
        return widget.attr("id");
    }
    
    function getPageNavigationUrl(baseQueryString, pageNumber, pageQueryParameter) {
        var paramDelimeter = "&";
        if (baseQueryString.indexOf("?") < 0) {
            paramDelimeter = "?";
        }
    
        var url = baseQueryString + paramDelimeter + pageQueryParameter + "=" + pageNumber;
    
        return url;
    }

    /**
     * Print table and line chart in the separate tabs
     */
    function printTableAndChart(table, initialTable) {
        var chartLabel = initialTable.columns[0];
        var columnLabels = initialTable.columns.slice(1); // don't include label of first column
        printLineChart(initialTable.rows, columnLabels, chartLabel);

        printTable(table, true);
    }
    
    function printLineChart(rows, columnLabels, chartLabel) {
        var chartId = "line-chart-" + analytics.util.getRandomNumber();
        print("<div class='chart-container'>");
        print("<div class='chart-label'>" + chartLabel + "</div>");
        print("<div class='line-chart' id='" + chartId + "'></div>");
        print("</div>");
        
        // display chart after the loading container
        print("<script>");
        print("  jQuery(function() { ");
        print("       analytics.views.lineChart.display('"
                + JSON.stringify(rows) + "', '"
                + JSON.stringify(columnLabels) + "', '"
                + chartId + "');");
        print("  });");
        print("</script>");
    }

    
    /**
     * Uses jQuery element widget linked with target container
     */
    function print(html) {
        viewHtml += html;
    }
    
    function setWidget(newWidget) {
        widget = newWidget;
    };
    
    function setParams(newParams) {
        params = newParams;
    };   

    function getParams() {
        return params;
    };    
    
    function clear() {
        widget.empty();
        viewHtml = "";
    };
    
    function show() {
        widget.html(viewHtml);
    };
    
    function showAbortMessage() {
        viewHtml = ABORT_LOADING_MESSAGE;
        show();
    };

    function showInterruptMessage() {
        viewHtml = INTERRUPT_LOADING_MESSAGE;
        show();
    };
    
    function showErrorMessage(status, textStatus, errorThrown) {
        if (status == 500) {
            viewHtml = "<div class='internalServerErrorMessage'>The data cannot be calculated.</div>";            
        } else {
            viewHtml = "<i>Error of loading data</i>: (" + status + ") '" + errorThrown + "'.";
        }
        
        show();
    };

    function getSystemMessageLabel(message) {
        return "<div class='system'>(" + message + ")</div>";
    }
    
    /**
     * Update UI according to the user UI preferences
     */
    function implementUIPreferences() {
        // verify if there is UI preferences button group at the current table
        if (! $("#ui-preferences").doesExist()) {
            return;
        }
        
        var displayTable = analytics.util.getGlobalParamFromStorage("display_table");
        if (displayTable == "true") {
            displayAllTables();
        } else {
            hideAllTables();
        }

        
        var displayChart = analytics.util.getGlobalParamFromStorage("display_chart");
        if (displayChart == "true") {
            displayAllCharts();
        } else {
            hideAllCharts();
        }
    }
    
    function displayAllTables() {
        var allTables = $(".table-container");        
        for (var i = 0; i < allTables.length; i++) {
            jQuery(allTables[i]).show();            
        }
    }

    function hideAllTables() {
        var allTables = $(".table-container");        
        for (var i = 0; i < allTables.length; i++) {
            jQuery(allTables[i]).hide();            
        }
    }
    
    function displayAllCharts() {
        var allCharts = $(".chart-container");        
        for (var i = 0; i < allCharts.length; i++) {
            jQuery(allCharts[i]).show();            
        }
    }
    
    function hideAllCharts() {
        var allCharts = $(".chart-container");        
        for (var i = 0; i < allCharts.length; i++) {
            jQuery(allCharts[i]).hide();            
        }
    }
    
    
    /** ****************** API ********** */
    return {
        setWidget: setWidget,
        setParams: setParams,
        getParams: getParams,
        clear: clear,
        show: show,
        print: print,
        getSystemMessageLabel: getSystemMessageLabel,
        
        // table
        printTable: printTable,
        loadTableHandlers: loadTableHandlers,
        printTableVerticalRow: printTableVerticalRow,

        // widget header
        printWidgetHeader: printWidgetHeader,
        
        // page navigation
        printBottomPageNavigator: printBottomPageNavigator,

        // line chart
        printTableAndChart: printTableAndChart,
        printLineChart: printLineChart,
        
        // server ajax error messages
        showAbortMessage: showAbortMessage,
        showInterruptMessage: showInterruptMessage,
        showErrorMessage: showErrorMessage,

        implementUIPreferences: implementUIPreferences,
    }
}