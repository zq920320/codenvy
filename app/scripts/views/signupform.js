define(["jquery","underscore","backbone","models/account","validation"],

    function($,_,Backbone,Account){


        /*
            Custom validator for .codenvy.com tenant names
        */

        jQuery.validator.addMethod("validDomain", function(value, element) {
            return Account.isValidDomain(value);
        });

        var ProgressiveForm = Backbone.View.extend({
            initialize : function(){

                this.canCollapse = true;

                this.$(".field > input").on("focus", _.bind(function(){
                    this.adjustFormDisplay();
                },this));

                this.$(".field > input").on("blur", _.bind(function(){
                    this.adjustFormDisplay();
                },this));

                $(this.el).submit(_.bind(function(){
                    this.stopFromCollapsing();
                    return false;
                },this));
            },

            adjustFormDisplay : function(){
                if(this.$(".field > input").toArray().indexOf(document.activeElement) !== -1){
                    this.$(".field > .signup-button").removeClass("hidden-button");
                    this.$(".field > .domain-name").removeClass("hidden-text-box");
                    $(".aternative-login").addClass("collapsed");
                } else {
                    if(this.canCollapse){
                        this.$(".field > .signup-button").addClass("hidden-button");
                        this.$(".field > .domain-name").addClass("hidden-text-box");
                        $(".aternative-login").removeClass("collapsed");
                    }
                }
            },

            stopFromCollapsing : function(){
                this.canCollapse = false;
            }
        });


    	var SignupForm = ProgressiveForm.extend({

            settings : {
                noDomainErrorMessage : "Please specify a domain name",
                noEmailErrorMessage : "You forgot to give us your email",
                invalidEmailErrorMessage : "Your email address must be legit",
                invalidDomainNameErrorMessage : "Please specify a valid name for the domain"
            },

            initialize : function(arguments){

                ProgressiveForm.prototype.initialize.apply(this,arguments);

                this.validator = $(this.el).validate({

                    rules: {
                        domain: {
                            required : true,
                            validDomain : true
                        },
                        email: {
                            required: true,
                            email: true
                        }
                    },

                    messages: {
                        domain: {
                            required : this.settings.noDomainErrorMessage,
                            validDomain : this.settings.invalidDomainNameErrorMessage
                        },
                        email: {
                            required: this.settings.noEmailErrorMessage,
                            email: this.settings.invalidEmailErrorMessage
                        }
                    },

                    onfocusout : false,
                    onkeyup : false,

                    submitHandler: _.bind(this.__submit,this),

                    // invalidHandler: function(form, validator){
                    //     console.log("invalid handler", validator);
                    // },

                    showErrors : _.bind(function(errorMap, errorList){
                        this.__showErrors(errorMap, errorList);
                    },this)
                });
            },

            __submit : function(form){

                this.stopFromCollapsing();

                Account.signUp(
                    $(form).find("input[name='email']").val(),
                    $(form).find("input[name='domain']").val()
                );
                //form.submit();
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
            }
    	});

    	return {
    		get : function(form){
    			if(typeof form === 'undefined'){
    				throw new Error("Need a form");
    			}

    			return new SignupForm({ el : form });
    		}
    	};

    }
);
