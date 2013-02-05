define(["jquery","underscore","backbone","models/account","validation"],

    function($,_,Backbone,Account){

        /*
            Custom validator for .codenvy.com tenant names
        */

        jQuery.validator.addMethod("validDomain", function(value) {
            return Account.isValidDomain(value+".codenvy.com");
        });


        var AccountFormBase = Backbone.View.extend({

            settings : {
                noDomainErrorMessage : "Please specify a domain name",
                noEmailErrorMessage : "You forgot to give us your email",
                noPasswordErrorMessage : "Please provide your account password",
                invalidEmailErrorMessage : "Your email address must be legit",
                invalidDomainNameErrorMessage : "Please specify a valid name for the domain"
            },

            initialize : function(){

                $(this.el).on('submit', function(e){
                    e.preventDefault();
                });

                this.validator = $(this.el).validate({

                    rules: this.__validationRules(),

                    messages: {
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
                        }
                    },

                    onfocusout : false, onkeyup : false,

                    submitHandler: _.bind(this.__submit,this),

                    showErrors : _.bind(function(errorMap, errorList){
                        this.__showErrors(errorMap, errorList);
                    },this)
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
                        email: true
                    }
                };
            },

            __showErrors : function(errorMap, errorList){
                console.log("this is", this);
                console.log("invalid form", errorMap, errorList);

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
