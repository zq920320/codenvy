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
 
 define(["jquery", "backbone"],function($,Backbone){

    var ErrorReport = Backbone.View.extend({

        show : function(message){
            if(typeof message === 'undefined'){
                throw new Error("Need a message");
            }

            $(this.el).html(message).removeClass("ok-message").addClass("expanded");
        },

        hide : function(){
            $(this.el).html(null).removeClass("expanded");
        }

    });

    return {
        get : function(dom){
            if(typeof dom === 'undefined'){
                throw new Error("Need a DOM element");
            }

            return new ErrorReport({el:dom});
        },

        ErrorReport : ErrorReport
    };

});
