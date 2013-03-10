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