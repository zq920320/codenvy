define(["models/account","views/form","validation"],

    function(Account,Form){

        /*
            Custom validator for .codenvy.com tenant names
        */

        jQuery.validator.addMethod("validDomain", function(value) {
            return Account.isValidDomain(value+".codenvy.com");
        });


        var AccountFormBase = Form.extend({

            settings : {
                noDomainErrorMessage : "Please specify a domain name.",
                noEmailErrorMessage : "Please provide an email address.",
                noPasswordErrorMessage : "Please provide your account password.",
                noConfirmPasswordErrorMessage : "Please type your new password again. Both passwords must match.",
                invalidEmailErrorMessage : "You must provide a valid email address.",
                invalidDomainNameErrorMessage : "Your domain name cannot start with a number and must only contain digits and characters. "
            },

            __validationRules : function(){
                return {
                    domain: {
                        required : true,
                        validDomain : true
                    },
                    email: {
                        required: true,
                        email: true
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
                        required: this.settings.noEmailErrorMessage,
                        email: this.settings.invalidEmailErrorMessage
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

            __showErrors : function(errorMap, errorList){
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
            },

            __restoreForm : function(){
                this.$(".working").addClass("hidden");
                this.$("input[type='submit']").show();
            },

            __showProgress : function(){
                this.$("input[type='submit']").hide();
                this.$(".working").removeClass("hidden");
            }
        });

        return AccountFormBase;
    }
);
