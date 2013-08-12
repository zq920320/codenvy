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
 
define(["jquery","models/maintenance","backbone"], function($,Maintenance,Backbone){

        var MaintenancePage = Backbone.View.extend({
            initialize : function(){
                this.$("#speedup").click(function(){Maintenance.speedUp(
                    Maintenance.getUserQueueInfo(function (data){
                        this.$("#queuePosition").html(data.queue);
                        this.$("#readyTime").html(data.readyTime);
                    }),

                    function (){
                        this.$("#first").html("The Codenvy IDE service site is currently down for maintenance and will be back up shortly. Sorry for the inconvenience.");
                        this.$("#second").remove();

                        }
                    );
                });
                Maintenance.getUserQueueInfo(function (data){
                    this.$("#queuePosition").html(data.queue);
                    this.$("#readyTime").html(data.readyTime);

                },
                function (){
                    this.$("#first").html("The Codenvy IDE service site is currently down for maintenance and will be back up shortly. Sorry for the inconvenience.");
                    this.$("#second").remove();

                }

                );
            }

        });
        
        return {

            get : function(div){
                if(typeof div === 'undefined'){
                    throw new Error("Need an element");
                }
                return new MaintenancePage({el:div});
            },

            MaintenancePage : MaintenancePage
        };
    }
);