define(["models/account","backbone"], function(Account,Backbone){

    var ErrorResponse = Backbone.View.extend({
        setError : function(){
            this.el.innerHTML = decodeURIComponent(Account.getQueryParameterByName("message"));
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