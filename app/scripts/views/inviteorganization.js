/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
 
define(["models/account","backbone"], function(Account,Backbone){

    // set {name} of organization from window.location
    
    var InviteOrganization = Backbone.View.extend({
        setOrganization : function(){
            this.el.innerHTML=("<b>" + Account.getQueryParameterByName("organization") + "</b>");
        }
    });

    return {
        get : function(el){
            if(typeof el === 'undefined'){
                throw new Error("Need a element");
            }
            return new InviteOrganization({el:el});
        },

        InviteOrganization : InviteOrganization
    };

});