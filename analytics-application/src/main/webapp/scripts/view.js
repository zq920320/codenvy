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
        print('<link href="css/database-table.css" rel="stylesheet" type="text/css" />');	    
	    print('<table cellspacing="0" class="database-table" align="center">');
	    print('<thead aria-hidden="false">');
	    print('<tr>');
	
	    // print first cell of header
	    if (table.columns.length > 0) {
	        print('<th class="header">');
	        print(table.columns[0]);
	        print('</th>');
	    }
	
	    // print other cells of header    
	    for (var i = 1; i < table.columns.length; i++) {
	        print('<th class="header">');
	        print(table.columns[i]);
	        print('</th>');
	    }
	    print('</tr>');
	    print('</thead>');
	
	    // print table body
	    print('<tbody>');
	    for (var i = 0; i < table.rows.length; i = i + 2) {
	        // print odd row
	        print('<tr>');
	
	        var firstCellClass = "cell";
	        if (typeof isDisplaySpecificFirstCell != "undefined" && isDisplaySpecificFirstCell) {
	            firstCellClass += " first-cell text-cursor";
	        }
	
	        // print first cell
	        print('<td class="' + firstCellClass + '">');
	        print('<div style="outline-style: none;" tabindex="0">');
	        print(table.rows[i][0]);
	        print('</div>');
	        print('</td>');
	
	        // print another cells
	        for (var j = 1; j < table.columns.length; j++) {
	            print('<td class="cell">');
	            print('<div style="outline-style: none;" tabindex="0">');
	            print(table.rows[i][j]);
	            print('</div>');
	            print('</td>');
	        }
	        print('</tr>');
	
	        // print pair row
	        if (i < table.rows.length - 1) {
	            print('<tr class="pair-row">');
	
	            // print first cell
	            print('<td class="' + firstCellClass + '">');
	            print('<div style="outline-style: none;" tabindex="0">');
	            print(table.rows[i + 1][0]);
	            print('</div>');
	            print('</td>');
	
	            // print another cells
	            for (var j = 1; j < table.columns.length; j++) {
	                print('<td class="cell">');
	                print('<div style="outline-style: none;" tabindex="0">');
	                print(table.rows[i + 1][j]);
	                print('</div>');
	                print('</td>');
	            }
	
	            print('</tr>');
	        }
	    }
	
	    print('<tfoot aria-hidden="true" style="display: none;"></tfoot>');
	    print('</tbody>');
	    print('</table>');
	};
	
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
	
	//load handlers of table events
	function loadTableHandlers() {
	    print("<script src='scripts/views/database-table.js'></script>");
	    print("<script>");
	    print("  jQuery(function() { ");
	    print("       analytics.views.databaseTable.setupRowHandlers();");
	    print("       analytics.views.databaseTable.setupVerticalRowHandlers();");
	    print("  });");
	    print("</script>");
	}
	
	
	/**
	 * currentPageNumber is 1-based
	 * prints page navigator, meets the requirements: 1 ... 4 5 6 /7/ 9 10 11 ... 100
	 */
	function printBottomPageNavigator(pageCount, currentPageNumber, queryString, pageQueryParameter) {
	    if (typeof pageCount == "undefined" || pageCount <= 0) {
	        return;
	    }
	
	    print('<link href="css/page-navigator.css" rel="stylesheet" type="text/css" />');
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

	    print("<script src='scripts/views/page-navigation.js'></script>");
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
    
    /** ****************** API ********** */
	return {
	    setWidget: setWidget,
	    setParams: setParams,
	    getParams: getParams,
	    clear: clear,
	    show: show,
    	print: print,
    	
    	// table
    	printTable: printTable,
    	loadTableHandlers: loadTableHandlers,
    	printTableVerticalRow: printTableVerticalRow,
    	
    	// page navigation
    	printBottomPageNavigator: printBottomPageNavigator,
    	loadPageNavigationHandlers: loadPageNavigationHandlers,
    	
    	// server events
    	showAbortMessage: showAbortMessage,
    	showErrorMessage: showErrorMessage
	}
}
