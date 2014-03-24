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
analytics.views.databaseTable = new DatabaseTable();

function DatabaseTable() {  
   var lastClickedRow;
   var lastMouseoveredRow;
     
   var MOUSEOVER_ROW_STYLE = " mouseover-row";
   var CLICKED_ROW_STYLE = " clicked-row";
 
   var setupHorizontalTableRowHandlers = function(displaySorting) {
      var tables = document.getElementsByClassName("database-table");
      if (tables != null) {      
         for(var i = 0; i < tables.length; i++) {
            var table = tables[i];
            var rows = table.rows;
            
            // don't take into account header rows
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
            
            if (displaySorting) {
                makeTableSortable(table);
            }
         }
      }
   };
   
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
    * Make table sortable by using tablesorter
    */
   function makeTableSortable(table) {
       jQuery(table).tablesorter({
           "cssAsc": "ascending",
           "cssDesc": "descending",
           "cssHeader": "unsorted",
       });
   }
   
	
   /** ****************** API ********** */
	return {
        setupHorizontalTableRowHandlers: setupHorizontalTableRowHandlers,
		setupVerticalTableRowHandlers: setupVerticalTableRowHandlers,
	}
}