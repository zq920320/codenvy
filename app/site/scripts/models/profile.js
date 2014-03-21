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
 
(function(){

    define(["jquery","underscore","backbone"], function($,_,Backbone){

        var Profile = Backbone.Model.extend({

            idAttribute: "", // set id = '' to use simple urlRoot without /id

            parse : function(response){ // parsing server response

            return response;
            },
            
			urlRoot : "/api/profile",

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
