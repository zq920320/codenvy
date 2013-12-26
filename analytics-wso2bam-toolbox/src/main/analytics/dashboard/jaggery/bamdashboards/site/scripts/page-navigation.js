var pageNavigation = new PageNavigation();

function PageNavigation() {  
   this.setupHandlers = function(handler) {
      var pageLinks = jQuery(".bottom-page-navigator a.page-link");
      if (pageLinks != null) {      
         for(var i = 0; i < pageLinks.length; i++) {
        	var pageLink = pageLinks[i];
            
            // add click event handler
            pageLink.addEventListener("click", function(event) {
            	event.preventDefault();
            	onLinkClick(this, handler);
            }, false);
         }
      }
   }
   
   function onLinkClick(linkElement, handler) {
	   handler.call(this, linkElement);
   }
}