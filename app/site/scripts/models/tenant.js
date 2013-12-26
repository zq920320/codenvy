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
 
(function(window){

    define(["jquery","underscore","backbone"], function($,_,Backbone){

        // implementation based on :
        // https://github.com/codenvy/cloud-ide/blob/8fe1e50cc6434899dfdfd7b2e85c82008a39a880/cloud-ide-war/src/main/webapp/js/select-tenant.js

        var Tenant = Backbone.Model.extend({

            initialize : function(){
                this.set("url",this.__buildTenantUrl());
            },

            __buildTenantUrl : function(){
                return window.location.protocol +
                    "//" + window.location.host + "/ide/" + this.get("name") + location.search.substr(0);
            }

        });

        var Tenants = Backbone.Collection.extend({
            url : "/api/workspace",
            model : Tenant,
            parse : function(response){
                return _.map(_.filter(response, function(r){
                    return r.temporary===false;
                }), function(r){
                    return { name : r.name, owner : r.owner.id };
                });
            },
            fetch : function(options){
                var dfd = $.Deferred();
                $.when(Backbone.Collection.prototype.fetch.apply(this,options))
                    .done(_.bind(function(){
                        dfd.resolve(this.models);
                    },this))
                    .fail(_.bind(function(){
                        dfd.reject(this);
                    },this));
                return dfd.promise();
            }
        });

        return {
            getTenants : function(){
                return new Tenants().fetch();
            },

            Tenant : Tenant,
            Tenants : Tenants
        };

    });

}(window));
