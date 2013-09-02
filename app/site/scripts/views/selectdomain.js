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
 
define(["jquery","underscore",
        "models/account","backbone", "handlebars",
        "text!templates/tenant.html","models/profile"],

    function($,_,Account,Backbone,Handlebars,tenantTemplate,Profile){

        var DomainSelector = Backbone.View.extend({

            tenantTemplate : Handlebars.compile(tenantTemplate),

            initialize : function(options){
                options = options || {};

                if(typeof options.el === 'undefined'){
                    throw new Error("Need an element");
                }

                Account.getTenants(
                    _.bind(this.onGotTennants,this),
                    _.bind(this.onErrorGettingTennants,this),
                    _.bind(this.onRedirect,this)
                );
            },

            onRedirect : function(d){
                var queryParam = window.location.search;
                window.location = d.url + queryParam;
            },

            onReceiveUserInfo : function(tenants,user){

                    _.each(tenants,function(tenant){

                        if (tenant.owner === user.account[0].id) {
                            this.$(".domain-list").append(
                                this.tenantTemplate(tenant.toJSON())
                            );
                        } else {
                            this.$(".shared-list").append(
                                this.tenantTemplate(tenant.toJSON())
                            );                            
                        }

                    },this);

            },

            onGotTennants : function(tenants){

                $(this.el).removeClass("loading");
                $.when(Profile.getUser()).done(function(user){
                   onReceiveUserInfo(tenants,user);
                }).fail(function(msg){
                    error([
                        new  AccountError(null,msg)
                    ]);
                });

            },

            onErrorGettingTennants : function(errors){
                if(errors.legth !== 0){
                    this.$(".error").html(
                        errors[0].getErrorDescription()
                    ).removeClass("hidden");
                }
            }

        });

        return {
            get : function(el){
                return new DomainSelector({ el : el });
            },

            DomainSelector : DomainSelector
        };
    }
);
