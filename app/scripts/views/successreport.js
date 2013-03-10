define(["jquery", "backbone"],function($,Backbone){

    var SuccessReport = Backbone.View.extend({

        show : function(message){
            if(typeof message === 'undefined'){
                throw new Error("Need a success message");
            }

            $(this.el).html(message).addClass("expanded ok-message");
        },

        hide : function(){
            $(this.el).html(null).removeClass("expanded ok-message");
        }

    });

    return {
        get : function(dom){
            if(typeof dom === 'undefined'){
                throw new Error("Need a DOM element");
            }

            return new SuccessReport({el:dom});
        },

        SuccessReport : SuccessReport
    };

});