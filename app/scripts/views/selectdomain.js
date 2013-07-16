define(["jquery","underscore",
        "models/account","backbone", "handlebars",
        "text!templates/tenant.html"],

    function($,_,Account,Backbone,Handlebars,tenantTemplate){

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

            onGotTennants : function(tenants){

                $(this.el).removeClass("loading");

                _.each(tenants,function(tenant){
                    this.$(".domain-list").append(
                        this.tenantTemplate(tenant.toJSON())
                    );
                },this);
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
