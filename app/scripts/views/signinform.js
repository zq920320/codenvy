(function(window){
    define(["jquery", "underscore", "views/accountformbase", "models/account"],

        function($,_,AccountFormBase,Account){

            var SignInForm = AccountFormBase.extend({

                initialize : function(attributes){
                    AccountFormBase.prototype.initialize.apply(this,attributes);

                    //bind onclick to Google and GitHub buttons

                    $(".oauth-button.google").click(function(){
                        Account.loginWithGoogle("Login page", function(url){
                            window.location = url;
                        });
                    });

                    $(".oauth-button.github").click(function(){
                        Account.loginWithGithub("Login page", function(url){
                            window.location = url;
                        });
                    });
                },

                __validationRules : function(){
                    return {
                        password : {
                            required: true
                        },
                        email: {
                            required: true,
                            email: true
                        }
                    };
                },

                __submit : function(form){
                    Account.login(
                        this.$("input[name='email']").val(),
                        this.$("input[name='password']").val(),
                        _.bind(function(data){
                            $(this.el).attr('action',data.loginUrl);
                            form.submit();
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

                    return false;
                }

            });

            return {
                get : function(form){
                    if(typeof form === 'undefined'){
                        throw new Error("Need a form");
                    }

                    return new SignInForm({
                        el : form
                    });
                },

                SignInForm : SignInForm
            };
        }
    );
}(window));
