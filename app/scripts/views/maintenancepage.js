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