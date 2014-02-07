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
analytics.views.topMenu = new TopMenu();

function TopMenu() { 
    var menu;
    
    function addHandlersToHidePopupMenu() {
        jQuery("#topmenu").hover(function() {
            hidePopupMenu();
        });
        
        jQuery(document).click(function() {
            hidePopupMenu();
        });
        
        // add handler of pressing "Esc" button
        jQuery(document).keydown(function(event) {
           var escKeyCode = 27;
           if (event.which == escKeyCode) {
               hidePopupMenu();
           }
        });
    }
    
    function turnOnNavButtons() {
        var buttons = jQuery( "a.nav" );
        for (var i = 0; i < buttons.length; i++) {
            jQuery(buttons[i])
            .button()
            .hover(function() {
                hidePopupMenu();
            });
        }
    }
    
    function turnOnDropdownButton(selectButtonId, displayTriangleIcon) {
        var buttonText = {};
        
        if (displayTriangleIcon) {
            buttonText = {
                text: false,
                icons: {
                    primary: "ui-icon-triangle-1-s"
                }
            };
        }
        
        jQuery( "#" + selectButtonId )
        .button(buttonText)
        .hover(function() {
            return displayPopupMenu(this)
        })
        .click(function() {
            return displayPopupMenu(this)
        });
    }
    
    function turnOnButtonSet(buttonSetId) {
        var buttons = jQuery( "#" + buttonSetId + " button" );
        for (var i = 0; i < buttons.length; i++) {
            jQuery(buttons[i])
            .button()
            .click(function() {
                console.log(this.name) + ":" + console.log(this.value);
            });
        }        
    }
    
    function displayPopupMenu(button) {
        hidePopupMenu();
        
        menu = $( button ).parent().next().show().position({
            my: "left top",
            at: "left bottom",
            of: button
        });
        
        menu.mouseleave(function() {
            hidePopupMenu();
        });
        
        $( button ).parent().parent().mouseleave(function() {
            hidePopupMenu();
        });
        
        return false;
    }
    
    function selectMenuItem(menuItemId) {
        if (typeof menuItemId != "undefined" && menuItemId != "null") {
            var menuItem = jQuery("#" + menuItemId);            
            if (menuItem.doesExist()) {
                menuItem.addClass("selected");
            }
        }
    }
    
    function hidePopupMenu() {
        if (typeof menu != "undefined" && menu != null) {
            menu.hide();
            menu = null;
        }
    }
    
    /** ****************** library API ********** */
    return {
        turnOnNavButtons: turnOnNavButtons,
        turnOnDropdownButton: turnOnDropdownButton,
        selectMenuItem: selectMenuItem,
        addHandlersToHidePopupMenu: addHandlersToHidePopupMenu,
        turnOnButtonSet: turnOnButtonSet,
    }
}