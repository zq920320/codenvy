(function(){

    define(["jquery","underscore","backbone"], function($,_,Backbone){

        var Profile = Backbone.Model.extend({

            idAttribute: "", // set id = '' to use simple urlRoot without /id

            parse : function(response){ // parsing server response

            return response;
            },

			urlRoot : "/rest/private/profile/current",

            fetch : function(options){ // fetch is asynchronous function
            var dfd = $.Deferred();
                $.when(Backbone.Model.prototype.fetch.apply(this,options))
                    .done(_.bind(function(){
                        dfd.resolve(this);
                    },this))
                    .fail(_.bind(function(){
                        dfd.reject(this);
                    },this));
                return dfd.promise();
			},

            save : function(options){ // save is asynchronous function
            var dfd = $.Deferred();
                $.when(Backbone.Model.prototype.save.apply(this,options))
                    .done(_.bind(function(){
                        dfd.resolve(this);
                    },this))
                    .fail(_.bind(function(error){
                        dfd.reject(error.status + " ("+ error.statusText + ") " + " : " + error.responseText);
                    },this));
                return dfd.promise();
            }

        });

        return {
            /* Returns User object from server*/
            getUser : function(){
                return new Profile().fetch();
            },
            /* Save user's data to server */
            updateUser : function(user,success,error){
                var pr = new Profile(user);
                pr.save().done(success).fail(error);
            },

            Profile : Profile
        };

    });

}());
