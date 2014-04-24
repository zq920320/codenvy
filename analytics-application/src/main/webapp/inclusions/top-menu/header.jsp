<%-- 
 CODENVY CONFIDENTIAL
 ________________

 [2012] - [2014] Codenvy, S.A.
 All Rights Reserved.
 NOTICE: All information contained herein is, and remains
 the property of Codenvy S.A. and its suppliers,
 if any. The intellectual and technical concepts contained
 herein are proprietary to Codenvy S.A.
 and its suppliers and may be covered by U.S. and Foreign Patents,
 patents in process, and are protected by trade secret or copyright law.
 Dissemination of this information or reproduction of this material
 is strictly forbidden unless prior written permission is obtained
 from Codenvy S.A.. 
--%>
<style>
	.ui-button, .ui-button a {
	    color: white !important;
	    font-weight: bold !important;
	    font-size: 15px !important;
	    margin-right: 0px;
	}
	
	.ui-state-default {
	    background: none;
	    border: 0 none !important;
	}
	
	.selected {
	    background-color: #0076B1;
        color: white !important;
	}
	
	.ui-state-hover {
	    background-color: #08c;
	}
	
	.ui-corner-all {
	    border-radius: 0;
	}
	
	.analytics-label {
        color: #0076B1;
    }

    /* button set */    
    .button-set .ui-state-hover {
        background-color: red;
    }
    
    .button-set .selected {
        background-color: purple;
    }

    .button-set .ui-button {
        font-size: 14px !important;
    }
    
    
    /* use white "ui-icon-triangle-1-s" icon in user menu button */
    .ui-state-hover .ui-icon, 
    .ui-state-focus .ui-icon,
    .ui-state-default .ui-icon {
        background-image: url(/analytics/images/ui-icons_ffffff_256x240.png); 
    }
</style>