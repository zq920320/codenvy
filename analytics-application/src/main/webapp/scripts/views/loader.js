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
analytics.views.loader = new Loader();

function Loader() {
    /**
	 * Loader
	 */
	var loader = jQuery("#loader");
	if (! loader.doesExist()) {
	   jQuery("body").append(
	      '<div id="loader">'
	      + '<div class="loader-container"></div>'
	      + '<table class="full-window-container">'
	      + '   <tr>'
	      + '     <td align="center">'
	      + '        <div id="loader-img">'
	      + '           <img src="/analytics/images/loader.gif" />'
	      + '         </div>'
	      + '     </td>'
	      + '   </tr>'
	      + '</table>'
	      + '</div>');
	   
	   loader = jQuery("#loader");
	   
	   // add handler of pressing "Esc" button
	   jQuery(document).keydown(function(event) {
	      var escKeyCode = 27;
	      if (event.which == escKeyCode) {
	         hide();
	      }
	   });
	}
	
	
	function show() {
	   loader.show();
	}
	
	function hide() {
	   loader.hide();
	}
	
    /** ****************** API ********** */
    return {
        show: show,
        hide: hide
    }
}