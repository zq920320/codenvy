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

	
    function printTable(table, isDisplaySpecificFirstCell) {
        print("<div>");
	    print('<table cellspacing="0" class="database-table" align="center">');
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
	 * Make table cells of column with certain name as linked with link = "columnLinkPrefix + {columnValue}"
	 */
    function makeTableColumnLinked(table, columnName, columnLinkPrefix) {
        var columnIndex = analytics.util.getColumnIndexByColumnName(table.columns, columnName);
        if (columnIndex != null) {        
            for (var i = 0; i < table.rows.length; i++) {
                var columnValue = table.rows[i][columnIndex];
                
                if (analytics.configuration.isSystemMessage(columnValue)) {
                   table.rows[i][columnIndex] = getSystemMessageLabel(columnValue);    
                } else {
                   var href = columnLinkPrefix + "=" + encodeURIComponent(columnValue);
                   table.rows[i][columnIndex] = "<a href='" + href + "'>" + columnValue + "</a>";
                }
            }
        }
    
        return table;
    }
	
	/**
	 * Load handlers of table events.
	 * Default value of displaySorting is true.
	 */
	function loadTableHandlers(displaySorting, sortingParams) {
	    var sortingParams = sortingParams || {};
	    if (typeof displaySorting == "undefined") {
	        displaySorting = true;
	    }
	    
	    print("<script>");
	    print("  jQuery(function() { ");
	    print("       analytics.views.databaseTable.setupHorizontalTableRowHandlers(" + displaySorting + ", '" + JSON.stringify(sortingParams) + "');");
	    print("       analytics.views.databaseTable.setupVerticalTableRowHandlers();");
	    print("  });");
	    print("</script>");
	}
	
	/**
	 * Prints page navigator, meets the requirements: 1 ... 4 5 6 /7/ 9 10 11 ... 100.
	 * CurrentPageNumber is 1-based. 
	 */
	function printBottomPageNavigator(pageCount, currentPageNumber, queryString, pageQueryParameter) {
	    if (typeof pageCount == "undefined" || pageCount <= 0) {
	        return;
	    }
	
	    print('<link href="/analytics/css/page-navigator.css" rel="stylesheet" type="text/css" />');
	    print("<div class='bottom-page-navigator'>");
	
	    for (var i = 1; i < pageCount + 1; i++) {
	        var href = getPageNavigationUrl(queryString, i, pageQueryParameter);
	
	        if (i == currentPageNumber) {
	            print("<a class='page-link current' href='" + href + "'>" + i + "</a>");
	
	        } else if (i == 1) {
	            print("<a class='page-link' href='" + href + "'>" + i + "</a>");
	            if (currentPageNumber > 4            		
	            		&& pageCount > 5) {   // don't display "..." if pageCount < (2 * 3)
	                print(' ... ')
	            }
	
	        } else if (i == pageCount) {
	            if (currentPageNumber < pageCount - 4 
	            		&& pageCount > 5) {   // don't display "..." if pageCount < (2 * 3)
	                print(' ... ')
	            }
	            print("<a class='page-link' href='" + href + "'>" + i + "</a>");
	
	        } else if (i + 3 >= currentPageNumber && currentPageNumber >= i - 3) {
	            print("<a class='page-link' href='" + href + "'>" + i + "</a>");
	        }
	    }
	    print("</div>");
	}
	
	
	// load page navigation handlers
	function loadPageNavigationHandlers(pageNavigatorLinkClickHandlerName) {
	    var pageNavigatorLinkClickHandler;
		if (typeof pageNavigatorLinkClickHandlerName != "undefined") {
			pageNavigatorLinkClickHandler = "function(pageNavigationLinkElement) {" 
				   + pageNavigatorLinkClickHandlerName + "(pageNavigationLinkElement, '" + getWidgetId() + "'); " 
		        + "}";
		} else {
			pageNavigatorLinkClickHandler = "function() {}";
			
		}

	    print("<script src='/analytics/scripts/views/page-navigation.js'></script>");
	    print("<script>");
        print("  jQuery(function() { ");
        print("       analytics.views.pageNavigation.setupHandlers(" + pageNavigatorLinkClickHandler + ");");
        print("  });");
        print("</script>");
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
    
    function showErrorMessage(status, textStatus, errorThrown) {
        viewHtml = "<i>Error of loading data</i>: (" + status + ") '" + errorThrown + "'.";
        show();
    };

    function getSystemMessageLabel(message) {
        return "<div class='system'>(" + message + ")</div>";
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
    	makeTableColumnLinked: makeTableColumnLinked,

    	// widget header
    	printWidgetHeader: printWidgetHeader,
    	
    	// page navigation
    	printBottomPageNavigator: printBottomPageNavigator,
    	loadPageNavigationHandlers: loadPageNavigationHandlers,
    	
    	// server events
    	showAbortMessage: showAbortMessage,
    	showErrorMessage: showErrorMessage
	}
}
