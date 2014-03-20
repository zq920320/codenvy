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

            var AdminForm = AccountFormBase.extend({

                initialize : function(attributes){
                    AccountFormBase.prototype.initialize.apply(this,attributes);
                    
                },

                __validationRules : function(){
                    var rule = {};
                    rule.password = {required: true};
                    rule.email = {
                            required: true,
                            checkEmail : true,
                            email: true
                        };
                    
                    return rule;
                },

                __submit : function(){
                    Account.adminLogin(
                            $(this.el).find("input[name='email']").val(),
                            $(this.el).find("input[name='password']").val(),
                            Account.getQueryParameterByName('redirect_url'),
                            _.bind(function(d){
                                this.trigger("success",d);
                            },this),
                            _.bind(function(errors){
                                if(errors.length !== 0){
                                    $(this.el).find("input[name='password']").val("");
                                    $(this.el).find("input[name='password']").focus();
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

                    return new AdminForm({
                        el : form
                    });
                },

                AdminForm : AdminForm
            };
        }
    );
}());
