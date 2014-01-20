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
analytics.views.pageNavigation = new PageNavigation();

function PageNavigation() {  
   var setupHandlers = function(handler) {
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
   
   /** ****************** API ********** */
	return {
		setupHandlers: setupHandlers
	}
}