/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
function AnalyticsLoader() {
    /**
	 * Loader.
	 * @see loader image generator here: http://preloaders.net
     */
    var loaderElement = null;
	
	function show(loaderContainerId) {
	    var loaderId = loaderContainerId + "_loader";
	    if (loaderElement == null 
	        && jQuery("#" + loaderId).doesExist() == false) {
	       jQuery("#" + loaderContainerId).append(
	          '<div id="' + loaderId + '" class="loader-container">'
              + '    <div class="loader-text">Calculating...</div>'
	          + '    <div class="loader-image"><img src="/analytics/images/horizontal_loader.gif" /></div>'
	          + '</div>');
	       
	       loaderElement = jQuery("#" + loaderId);
	    }
	}
	
	function hide() {
	   if (loaderElement != null) {
           loaderElement.remove();
           loaderElement = null;
	   }
	}
	
    /** ****************** API ********** */
    return {
        show: show,
        hide: hide
    }
}
