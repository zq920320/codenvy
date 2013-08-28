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
 
define(["underscore", "views/accountformbase","models/account"],

    function(_,AccountFormBase,Account){

        var SetPasswordForm = AccountFormBase.extend({
            settings : {
                successChangingPassword : "Your password was changed successfully",
                noPasswordErrorMessage : "Please, enter a new password for your account",
                noConfirmPasswordErrorMessage : "Please type your new password again. Both passwords must match."

            },

            __validationRules : function(){
                return {
                    change_password: {
                        required : true
                    },
                    confirm_password: {
                        required: true,
                        equalTo: "input[name='change_password']"
                    }
                };
            },
            
            __validationMessages : function(){
                return {
                    change_password: {
                        required: this.settings.noPasswordErrorMessage
                    },
                    confirm_password: {
                        required: this.settings.noConfirmPasswordErrorMessage,
                        equalTo: this.settings.noConfirmPasswordErrorMessage
                    }
                };
            },

            __submit : function(){
                this.trigger("submitting");

                Account.changePassword(
                    this.$("input[name='change_password']").val(),
                    _.bind(function(){                        
                        this.$("input[name='change_password']").val("");
                        this.$("input[name='confirm_password']").val("");

                        this.trigger("success");
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
        });


        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }
                return new SetPasswordForm({el:form});
            },

            SetPasswordForm : SetPasswordForm
        };
    }
);