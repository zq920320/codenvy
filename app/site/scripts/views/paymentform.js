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
    define(["jquery", "underscore", "views/accountformbase", "models/account","validation"],

        function($,_,AccountFormBase,Account){
            var braintree;
            var PaymentForm = AccountFormBase.extend({

                initialize : function(){
                    /*global Braintree*/
                    braintree = Braintree.create("MIIBCgKCAQEAu4DIBsO0K0mkhvCsCAQzwjQo71bLswM1LQS3xCz+81UTOXShVH2YjgjA/P/S1NUuKxZe5ppku8F4Y7NHMPniod8KmChoNuUAnq9EE91BAqrj9OKTlNpxKMuXG6OTnF4EfAzz5yDI4p8vSfVHKNU6WvqySB16uw0a+iC8sDjoib6rUeSeniWpQBn/FeR7iVFVGHShkgvRs1SX2BjLnZOalhlI94yrPu3vNJd2Gk1YPgQBtAHbjhUtIcvpPAcFqcUQVaEavVVkPeEMGCaIsaR6LJvJ0K+r6K4t8ZcPzD6cA7ylM89nFPzGND4gLhxftd6p/R3QBPGGMWP5IGJDo/ThzwIDAQAB");
                    Account.addSubscription(
                            this.el,
                            _.bind(function(message){
                                this.trigger("success",message); //FIX message
                            },this),
                            _.bind(function(errors){
                                if(errors.length !== 0){
                                    /*$(this.el).find("input[name='password']").val("");
                                    $(this.el).find("input[name='password']").focus();*/
                                    this.trigger(
                                        "invalid",
                                        errors[0].getFieldName(),
                                        errors[0].getErrorDescription()
                                    );
                                }
                            },this)
                        );
                },

                __validationRules : function(){
/*                    var rule = {};
                    rule.password = {required: true};
                    rule.email = {required: true};
                    
                    return rule;*/
                },

                __submit : function(){
                    $("#submit").attr("disabled", "disabled");
                    braintree.encryptForm('codenvy-payment-form');
                    Account.paymentFormSubmit(
                            _.bind(function(message){
                                this.trigger("success",message);
                            },this),
                            _.bind(function(errors){
                                if(errors.length !== 0){
                                    /*$(this.el).find("input[name='password']").val("");
                                    $(this.el).find("input[name='password']").focus();*/
                                    this.trigger(
                                        "invalid",
                                        errors[0].getFieldName(),
                                        errors[0].getErrorDescription()
                                    );
                                }
                            },this)
                        );
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
}());
