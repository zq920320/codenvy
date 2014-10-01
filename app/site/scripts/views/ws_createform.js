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
    define(["jquery", "underscore", "views/accountformbase", "models/account","validation"],

        function($,_,AccountFormBase,Account){

            var WSCreateForm = AccountFormBase.extend({

                initialize : function(attributes){
                    Account.redirectIfUserHasLoginCookie();
                    AccountFormBase.prototype.initialize.apply(this,attributes);
                    Account.supportTab();
                    //bind onclick to Google and GitHub buttons
                    $(".oauth-button.google").click(function(){
                        Account.loginWithGoogle("Create WS page", function(url){
                            window.location = url;
                        });
                    });

                    $(".oauth-button.github").click(function(){
                        Account.loginWithGithub("Create WS page", function(url){
                            window.location = url;
                        });
                    });

                    $(".sign-in").click(function(){
                        var url = "/site/login" + window.location.search;
                        window.location = url;
                    });

                },

                __validationRules : function(){
                    return {
                        domain: {
                        required : true,
                        validDomain : true
                    },
                        email: {
                            required: true,
                            checkEmail : true,
                            email: true
                        }
                    };
                },

            __submit : function(form){
                if (Account.isWebsocketEnabled()) {
               
                this.trigger("submitting");

                Account.createTenant(
                    $(form).find("input[name='email']").val(),
                    $(form).find("input[name='domain']").val(),
                    _.bind(function(d){
                        this.trigger("success",d);
                    },this),
                    _.bind(function(errors){

                        this.__restoreForm();

                        if(errors.length !== 0){
                            this.trigger(
                                "invalid",
                                errors[0].getFieldName(),
                                errors[0].getErrorDescription()
                            );
                        }
                    },this)
                );
                }
                return false;
            }

            });

            return {
                get : function(form){
                    if(typeof form === 'undefined'){
                        throw new Error("Need a form");
                    }

                    return new WSCreateForm({
                        el : form
                    });
                },

                WSCreateForm : WSCreateForm
            };
        }
    );
}(window));
