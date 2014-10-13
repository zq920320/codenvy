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

analytics.views = analytics.views || {};
analytics.views.databaseTable = new AnalyticsDatabaseTable();

function AnalyticsDatabaseTable() {
   var lastClickedRow;
   var lastMouseoveredRow;
     
   var MOUSEOVER_ROW_STYLE = " mouseover-row";
   var CLICKED_ROW_STYLE = " clicked-row";
 
   /**
    * If table is without preset id, then tableId will be 'undefined'.
    */
   var setupHorizontalTableRowHandlers = function(displaySorting, sortingParamsObjectString, tableId) {
      if (tableId == "undefined") {
          var tables = document.getElementsByClassName("database-table");
          if (tables != null) {      
             for(var i = 0; i < tables.length; i++) {
                var table = tables[i];
                setupHorizontalTable(table, displaySorting, sortingParamsObjectString);
             }
          }
      } else {
          var table = document.getElementById(tableId);
          if (typeof table != "undefined") {
              setupHorizontalTable(table, displaySorting, sortingParamsObjectString);
          }
      }
   };

   var setupHorizontalTable = function(table, displaySorting, sortingParamsObjectString) {
       var rows = table.rows;
       
       // don't take into account header rows
       if (typeof rows != "undefined") {
           var rowCount = rows.length;       
    
           for (var j = 1; j < rowCount; j++) {
              var row = rows[j];
              
              // add click event handler
              row.addEventListener("click", onRowClick, true);
              
              // add mouse mouseover event handler
              row.addEventListener("mouseover", onRowMouseover, true);
              
              // add mouse mouseout event handler
              row.addEventListener("mouseout", onRowMouseout, true);
           }
       
           if (rowCount > 2 && displaySorting) {  // don't display sorting command if there is no rows or 
                                                  // if there is only one row in the table (don't take header row into account)
               var sortingParams = JSON.parse(sortingParamsObjectString);
               makeTableSortable(table, sortingParams);
           }
       }
   }
   
   var setupVerticalTableRowHandlers = function() {
      var tables = document.getElementsByClassName("database-table-vertical-row");
      if (tables != null) {      
         for(var i = 0; i < tables.length; i++) {
            var table = tables[i];
            var rows = table.rows;
            
            for (var j = 0; j < rows.length; j++) {
               var row = rows[j];
               
               // add click event handler
               row.addEventListener("click", onRowClick, true);
               
               // add mouse mouseover event handler
               row.addEventListener("mouseover", onRowMouseover, true);
               
               // add mouse mouseout event handler
               row.addEventListener("mouseout", onRowMouseout, true);
            }
         }
      }
   };
   
   function onRowClick(event) {
        var row = event.currentTarget;

        if (typeof lastClickedRow != "undefined") {
            lastClickedRow.className = lastClickedRow.className.replace(
                    CLICKED_ROW_STYLE, "");
        }

        if (typeof lastMouseoveredRow != "undefined") {
            lastMouseoveredRow.className = lastMouseoveredRow.className
                    .replace(MOUSEOVER_ROW_STYLE, "");
        }

        row.className += CLICKED_ROW_STYLE;

        lastClickedRow = row;
   }
   
   function onRowMouseover(event) {	
      var row = event.currentTarget;
      if (row != lastClickedRow) {
         row.className += MOUSEOVER_ROW_STYLE;
      }
      
      lastMouseoveredRow = row;
   }
   
   function onRowMouseout(event) {
      if (typeof lastMouseoveredRow != "undefined") {
         lastMouseoveredRow.className = lastMouseoveredRow.className.replace(MOUSEOVER_ROW_STYLE, "");       
      }
   }

   /**
    * Make table sortable by using DataTable plugin. 
    * Example of viewParams parameter value with number of column starting from 0:
    *   clientSortParams: {
    *       "ascSortColumnNumber": 1,
    *       "descSortColumnNumber": 3,
    *       "columnsWithoutSorting": ["_all"]
    *   }
    *   
    * "columnsWithoutSorting" can be:
- a string - class name will be matched on the TH for the column
- 0 or a positive integer - column index counting from the left
- a negative integer - column index counting from the right
- the string "_all" - all columns (i.e. assign a default)
    * 
    * @see http://www.datatables.net/ref , https://datatables.net/usage/columns
    */
   function makeTableSortable(table, viewParams) {
       var pluginParams = {};
              
       pluginParams.bRetrieve = true;
       pluginParams.aaSorting = [];
       pluginParams.aoColumnDefs = [];
       
       if (typeof viewParams != "undefined") {
           if (typeof viewParams.ascSortColumnNumber != "undefined") {
               pluginParams.aaSorting.push([viewParams.ascSortColumnNumber, "asc"]);
           }

           if (typeof viewParams.descSortColumnNumber != "undefined") {
               pluginParams.aaSorting.push([viewParams.descSortColumnNumber, "desc"]);
           }
           
           if (typeof viewParams.columnsWithoutSorting != "undefined") {
               pluginParams.aoColumnDefs.push(
                   {"bSortable": false, 
                    "aTargets": viewParams.columnsWithoutSorting, 
               }); 
           }
       }
       
       jQuery(table).dataTable(pluginParams);
   }
   
	
   /** ****************** API ********** */
	return {
        setupHorizontalTableRowHandlers: setupHorizontalTableRowHandlers,
		setupVerticalTableRowHandlers: setupVerticalTableRowHandlers,
	}
}
