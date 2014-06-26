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
 
define(["jquery","underscore","views/accountformbase","models/account", "handlebars",
        "text!templates/accountlist.html"], 

    function($,_,AccountFormBase,Account,Handlebars,accountList){
        var braintree;

        var PaymentForm = AccountFormBase.extend({

                accountListTemplate : Handlebars.compile(accountList),

                 __validationRules : function(){
                    var rule = {};
                    rule.expirationMonth = {required: true};
                    rule.expirationYear = {required: true};
                    
                    return rule;
                },

                __validationMessages : function(){
                    return {
                        expirationMonth: {
                            required: 'Expiration month required'
                        },
                        expirationYear: {
                            required: 'ExpirationYear required'
                        }
                    };
                },

                __submit : function(){
                    this.__showProgress();
                    this.trigger("submitting");
                    braintree.encryptForm('payment-form');
                    Account.paymentFormSubmit(
                            this.accountId,
                            _.bind(function(message){
                                this.trigger("success",message);
                            },this),
                            _.bind(function(errors){
                                if(errors.length !== 0){
                                     this.trigger(
                                        "invalid",
                                        errors[0].getFieldName(),
                                        errors[0].getErrorDescription()
                                    );
                                }
                            },this)
                        );
                },
                /*global Braintree*/
/*                addSubscription : function(){
                    
                    braintree = Braintree.create("MIIBCgKCAQEAu4DIBsO0K0mkhvCsCAQzwjQo71bLswM1LQS3xCz+81UTOXShVH2YjgjA/P/S1NUuKxZe5ppku8F4Y7NHMPniod8KmChoNuUAnq9EE91BAqrj9OKTlNpxKMuXG6OTnF4EfAzz5yDI4p8vSfVHKNU6WvqySB16uw0a+iC8sDjoib6rUeSeniWpQBn/FeR7iVFVGHShkgvRs1SX2BjLnZOalhlI94yrPu3vNJd2Gk1YPgQBtAHbjhUtIcvpPAcFqcUQVaEavVVkPeEMGCaIsaR6LJvJ0K+r6K4t8ZcPzD6cA7ylM89nFPzGND4gLhxftd6p/R3QBPGGMWP5IGJDo/ThzwIDAQAB");
                    Account.addSubscription(
                            this.el,
                            _.bind(function(accountId){
                                this.accountId = accountId;
                                $(this.el).removeClass('hidden');
                            },this),
                            _.bind(function(message){
                                this.trigger("success",message);
                            },this),
                            _.bind(function(errors){
                                if(errors.length !== 0){
                                    this.trigger(
                                        "invalid",
                                        errors[0].getFieldName(),
                                        errors[0].getErrorDescription()
                                    );
                                }
                            },this)
                        );                    
                },*/
                // get all Workspaces to pay for
                selectWorkspace : function(){
                    Account.getAccounts(
                        this,
                        _.bind(this.onGotWorkspaces,this),
                        _.bind(this.onErrorGettingWorkspaces,this),
                        _.bind(this.onRedirect,this)                        

                    );
                },

                onRedirect : function(d){
                    var queryParam = window.location.search;
                    window.location = d.url + queryParam;
                },                

                onGotWorkspaces : function(workspaces){
                    _.each(workspaces,_.bind(function(workspace){
                                $(".workspace-list").removeClass("hidden");
                                $(".workspace-list").append(
                                    this.accountListTemplate(workspace.attributes)
                                ).click(_.bind(function() {
                                    this.addSubscription(workspace.attributes.id);},this));
                    },this));                   

                },

                addSubscription : function(workspaceId){
                    /*global Braintree*/
                    $(".workspace-list").addClass('hidden');
                    braintree = Braintree.create("MIIBCgKCAQEAu4DIBsO0K0mkhvCsCAQzwjQo71bLswM1LQS3xCz+81UTOXShVH2YjgjA/P/S1NUuKxZe5ppku8F4Y7NHMPniod8KmChoNuUAnq9EE91BAqrj9OKTlNpxKMuXG6OTnF4EfAzz5yDI4p8vSfVHKNU6WvqySB16uw0a+iC8sDjoib6rUeSeniWpQBn/FeR7iVFVGHShkgvRs1SX2BjLnZOalhlI94yrPu3vNJd2Gk1YPgQBtAHbjhUtIcvpPAcFqcUQVaEavVVkPeEMGCaIsaR6LJvJ0K+r6K4t8ZcPzD6cA7ylM89nFPzGND4gLhxftd6p/R3QBPGGMWP5IGJDo/ThzwIDAQAB");
                    Account.addSubscription(
                            this.el,
                            workspaceId,
                            _.bind(function(accountId){
                                this.accountId = accountId;
                                $(this.el).removeClass('hidden');
                            },this),
                            _.bind(function(message){
                                this.trigger("success",message);
                            },this),
                            _.bind(function(errors){
                                if(errors.length !== 0){
                                    this.trigger(
                                        "invalid",
                                        errors[0].getFieldName(),
                                        errors[0].getErrorDescription()
                                    );
                                }
                            },this)
                        );                    
                },                

                onErrorGettingWorkspaces : function(errors){
                    if(errors.legth !== 0){
                        this.$(".error").html(
                            errors[0].getErrorDescription()
                        ).removeClass("hidden");
                    }
                }                

            });

            return {
                get : function(form){
                    if(typeof form === 'undefined'){
                        throw new Error("Need a form");
                    }

                    return new PaymentForm({
                        el : form
                    });
                },
                PaymentForm : PaymentForm
        };
    }
);
