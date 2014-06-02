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
	    font-size: 14px !important;
	}
	
	.ui-state-default {
	    background: none;
	    border: 0 none !important;
	}
	
	.selected .ui-button-text,
	.ui-state-hover .ui-button-text {
		border-bottom: 2px solid #54aee0;
		padding: 2px 3px;
        color: white;
	}
	
	.ui-corner-all {
	    border-radius: 0;
	}

    /* button set */    
    .button-set .ui-state-hover {}
    
    .button-set .ui-button {
        font-size: 14px;
    }
    
    .ui-button-text-only .ui-button-text {
        padding: 2px 3px;
        margin: 10px 15px;
    }
    
    /* use white "ui-icon-triangle-1-s" icon in user menu button */
    .ui-state-hover .ui-icon, 
    .ui-state-focus .ui-icon,
    .ui-state-default .ui-icon,
    .ui-state-default .ui-icon,
    .ui-state-default .ui-icon .selected  {
        background-image: url(/analytics/images/ui-icons_ffffff_256x240.png);
    }
    
    #topmenu-user span.ui-button-text {
        border: 0px;
        padding: 40% 0;
    }
    
    #data-universe li {
        padding: 3px 0px;
    }
</style>