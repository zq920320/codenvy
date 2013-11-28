var TimelineGadget = new TimelineGadget();

function TimelineGadget() {  
   // ****************** Private members 
   var lastClickedRow;
   var lastMouseoveredRow;
   
   var MOUSEOVER_ROW_STYLE = " mouseover-row";
   var CLICKED_ROW_STYLE = " clicked-row";

   // ****************** constructor
   this.setupRowHandlers = function(tableId) {
      var table = document.getElementById(tableId);
      if (table != null) {
         var rows = table.rows;
         if (rows.length > 2) {
            // don't take into account header and footer rows
            for (var i = 1; i < rows.length - 1; i++) {
               var row = rows[i];
               
               // add click event handler
               row.addEventListener("click", onRowClick, true);
               
               // add mouse mouseover event handler
               row.addEventListener("mouseover", onRowMouseover, true);
               
               // add mouse mouseout event handler
               row.addEventListener("mouseout", onRowMouseout, true);
            }
         }
      }
   }
   
   // ****************** Private methods ********** 
   function onRowClick(event) {
   	var row = event.currentTarget;
      
      if (typeof lastClickedRow != "undefined") {
   		lastClickedRow.className = lastClickedRow.className.replace(CLICKED_ROW_STYLE, "");
   	}
   	
      if (typeof lastMouseoveredRow != "undefined") {
         lastMouseoveredRow.className = lastMouseoveredRow.className.replace(MOUSEOVER_ROW_STYLE, "");       
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
}