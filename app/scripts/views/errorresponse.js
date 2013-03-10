define(["models/account"], function(Account){

    var ErrorResponse = Backbone.View.extend({
        setError : function(){
            this.el.innerHTML = decodeURIComponent(Account.getParam("message"));
        }
    });

    return {
        get : function(el){
            if(typeof el === 'undefined'){
                throw new Error("Need an element");
            }
            return new ErrorResponse({el:el});
        },

        ErrorResponse : ErrorResponse
    };

});