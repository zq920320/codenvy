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
    define(["jquery", "underscore", "views/accountformbase", "models/account", "handlebars", "text!templates/oauthbtn.html", "validation"],

        function($,_,AccountFormBase,Account, Handlebars, oauthTemplate){
            var oauthProviderButtons = ["google", "github"]; // the list of oauth provider buttons
            var WSCreateForm = AccountFormBase.extend({

                oauthTemplate : Handlebars.compile(oauthTemplate),

                initialize : function(attributes){
                    //Account.redirectIfUserHasLoginCookie();
                    if (Account.isLoginCookiePresent() && (Account.getQueryParameterByName('account') !== "new")){
                        window.location = '/site/login' + window.location.search;
                    }
                    AccountFormBase.prototype.initialize.apply(this,attributes);
                    //bind onclick to Google and GitHub buttons
                    Account.getOAuthproviders(_.bind(this.constructOAuthElements,this));
                    $("#signIn").click(function(){
                        $.cookie('logged_in', true, {path: "/"});
                        window.location = Account.appendQuery("/site/login");
                    });

                },

                constructOAuthElements : function(deffer){
                    var self = this;
                    deffer
                    .then(function(providers){
                        _.each(providers,function(provider){
                            if (oauthProviderButtons.indexOf(provider.name) >= 0){
                                self.$(".oauth-list").append(
                                    self.oauthTemplate(provider)
                                );
                                // bind action to oauth button
                                $(".oauth-button." + provider.name).click(function(){
                                    Account.loginWithOauthProvider(provider, "Create WS page", function(url){
                                        window.location = url;
                                    });
                                });
                            }
                        },this);
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
                    $(form).find("input[name='username']").val(),
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
