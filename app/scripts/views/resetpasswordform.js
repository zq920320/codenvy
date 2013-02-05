define(["views/accountformbase","models/account"],

    function(AccountFormBase,Account){

        var ResetPasswordForm = AccountFormBase.extend({

            initialize : function(attributes){
                AccountFormBase.prototype.initialize.apply(this,attributes);

                Account.confirmSetupPassword(
                    _.bind(function(d){
                        this.$(".email").html(d.email);
                    },this),
                    _.bind(function(errors){
                        if(errors.length !== 0){
                            this.trigger(
                                "invalid",
                                errors[0].getFieldName(),
                                errors[0].getErrorDescription()
                            );
                        }
                    },this)
                );
            },

            __validationRules : function(){
                return {
                    password: {
                        required : true
                    },
                    password2: {
                        required: true,
                        equalTo: "input[name='password']"
                    }
                };
            },

            __submit : function(){
                this.trigger("submitting");
                this.__showProgress();

                Account.setupPassword(
                    this.$("input[name='password']").val(),
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
        });


        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }
                return new ResetPasswordForm({el:form});
            },

            ResetPasswordForm : ResetPasswordForm
        };
    }
);
