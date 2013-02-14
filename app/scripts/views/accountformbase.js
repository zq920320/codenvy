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
                noDomainErrorMessage : "Please specify a domain name",
                noEmailErrorMessage : "You forgot to give us your email",
                noPasswordErrorMessage : "Please provide your account password",
                noConfirmPasswordErrorMessage : "Please type your new password again. Both passwords must match.",
                invalidEmailErrorMessage : "Your email address must be legit",
                invalidDomainNameErrorMessage : "Please specify a valid name for the domain"
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
                this.$("input[type='submit']").removeAttr("disabled");
            },

            __showProgress : function(){
                this.$("input[type='submit']").attr("disabled","disabled");
            }
        });

        return AccountFormBase;
    }
);
