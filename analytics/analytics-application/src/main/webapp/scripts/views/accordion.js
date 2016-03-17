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
if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.views = analytics.views || {};
analytics.views.accordion = new AnalyticsAccordion();

function AnalyticsAccordion() {
    /**
	 * Management of Accordion Widget of jQuery UI library 
	 * @see http://api.jqueryui.com/accordion/
     */
	
	function display(panelId, isOpened) {
        jQuery("#" + panelId).accordion({
            header: ".collabsiblePanelTitle",
            collapsible: true,
            active: ((isOpened) ? 0 : false),
        });
	}
	
	function close(panelId) {
	    jQuery("#" + panelId).accordion( "option", "active", false );
	}
	
    /** ****************** API ********** */
    return {
        display: display,
        close: close
    }
}
