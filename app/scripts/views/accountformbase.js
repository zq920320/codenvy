define(["jquery","underscore","backbone","models/account","validation"], 

	function($,_,Backbone,Account){

		/*
	        Custom validator for .codenvy.com tenant names
	    */

	    jQuery.validator.addMethod("validDomain", function(value, element) {
	        return Account.isValidDomain(value+".codenvy.com");
	    });


	    var AccountFormBase = Backbone.View.extend({

	    	settings : {
                noDomainErrorMessage : "Please specify a domain name",
                noEmailErrorMessage : "You forgot to give us your email",
                invalidEmailErrorMessage : "Your email address must be legit",
                invalidDomainNameErrorMessage : "Please specify a valid name for the domain"
            },
	    	
	    	initialize : function(attributes){

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
            },

	    	__submit : function(form){}
	    });

		return AccountFormBase;
	}
);