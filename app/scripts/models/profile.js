(function(){

    define(["jquery","underscore","backbone"], function($,_,Backbone){

        var Profile = Backbone.Model.extend({
        	parse : function(response){

        		return response;
        	},

			urlRoot : "/rest/private/profile/current",

            fetch : function(options){
			    var dfd = $.Deferred();
			    $.when(Backbone.Model.prototype.fetch.apply(this,arguments))
			        .done(_.bind(function(){
			            dfd.resolve(this);
			        },this))
			        .fail(_.bind(function(){
			            dfd.reject(this);
			        },this));
			    return dfd.promise();
			}
        });



        return {
            getUser : function(){
                return new Profile().fetch();
            },
            Profile : Profile
        };

    });

}());
