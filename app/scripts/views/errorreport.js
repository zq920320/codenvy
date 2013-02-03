define(["backbone"],function(Backbone){

    var ErrorReport = Backbone.View.extend({

        show : function(message){
            if(typeof message === 'undefined'){
                throw new Error("Need a message");
            }

            $(this.el).html(message).addClass("expanded");
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
