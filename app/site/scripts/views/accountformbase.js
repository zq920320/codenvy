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
 
define(["jquery","models/account","views/form","validation"],

    function($,Account,Form){

        /*
            Custom validator for codenvy.com workspace names
        */

        jQuery.validator.addMethod("validDomain", function(value) {
            return Account.isValidDomain(value.toLowerCase());
        });
        jQuery.validator.addMethod("checkEmail", function(value) {
            return Account.isValidEmail(value);
        });

        var AccountFormBase = Form.extend({

            settings : {
                noDomainErrorMessage : "Please specify a workspace name.",
                noEmailErrorMessage : "Please provide an email address.",
                noPasswordErrorMessage : "Please provide your account password.",
                noConfirmPasswordErrorMessage : "Please type your new password again. Both passwords must match.",
                invalidEmailErrorMessage : "Emails with '+' and '/' are not allowed",
                invalidDomainNameErrorMessage : "Your workspace name should start with a Latin letter or a digit, and must only contain Latin letters, digits, underscores, dots or dashes. You are allowed to use from 3 to 20 characters in a workspace name."
            },

            __validationRules : function(){
                return {
                    domain: {
                        required : true,
                        validDomain : true
                    },
                    email: {
                        required : true,
                        checkEmail : true
                    }
                };
            },

            __validationMessages : function(){
                return {
                    domain: {
                        required : this.settings.noDomainErrorMessage,
                        validDomain : this.settings.invalidDomainNameErrorMessage
                    },
                    email: {
                        required : this.settings.noEmailErrorMessage,
                        checkEmail : this.settings.invalidEmailErrorMessage
                    },
                    password: {
                        required: this.settings.noPasswordErrorMessage
                    },
                    password1: {
                        required: this.settings.noConfirmPasswordErrorMessage,
                        equalTo: this.settings.noConfirmPasswordErrorMessage
                    }
                };
            },

            __showErrors : function(errorMap){
                function refocus(el){
                    el.focus();
                }

                if(typeof errorMap.email !== 'undefined'){
                    this.trigger("invalid","email",errorMap.email);
                    refocus(this.$("input[name='email']"));
                    return;
                }

                if(typeof errorMap.domain !== 'undefined'){
                    this.trigger("invalid","domain",errorMap.domain);
                    refocus(this.$("input[name='email']"));
                    return;
                }

                if(typeof errorMap.password !== 'undefined'){
                    this.trigger("invalid","password",errorMap.password);
                    refocus(this.$("input[name='password']"));
                    return;
                }

                if(typeof errorMap.password1 !== 'undefined'){
                    this.trigger("invalid","password1",errorMap.password1);
                    refocus(this.$("input[name='password1']"));
                    return;
                }

                if(typeof errorMap.change_password !== 'undefined'){
                    this.trigger("invalid","change_password",errorMap.change_password);
                    refocus(this.$("input[name='change_password']"));
                    return;
                }

                if(typeof errorMap.confirm_password !== 'undefined'){
                    this.trigger("invalid","confirm_password",errorMap.confirm_password);
                    refocus(this.$("input[name='confirm_password']"));
                    return;
                }

                if(typeof errorMap.first_name !== 'undefined'){
                    this.trigger("invalid","first_name",errorMap.first_name);
                    refocus(this.$("input[name='first_name']"));
                    return;
                }
                if(typeof errorMap.last_name !== 'undefined'){
                    this.trigger("invalid","last_name",errorMap.last_name);
                    refocus(this.$("input[name='last_name']"));
                    return;
                }
                if(typeof errorMap.phone_work !== 'undefined'){
                    this.trigger("invalid","phone_work",errorMap.phone_work);
                    refocus(this.$("input[name='phone_work']"));
                    return;
                }
                if(typeof errorMap.company !== 'undefined'){
                    this.trigger("invalid","company",errorMap.company);
                    refocus(this.$("input[name='company']"));
                    return;
                }
                if(typeof errorMap.title !== 'undefined'){
                    this.trigger("invalid","title",errorMap.title);
                    refocus(this.$("input[name='title']"));
                    return;
                }
            },

            __restoreForm : function(){
                this.$("input[type='submit']").removeAttr("disabled");
            },

            __showProgress : function(){
                this.$("input[type='submit']").attr("disabled","disabled");
            }
        });

        return AccountFormBase;
    }
);
