(function(window){
    define(["jquery", "underscore", "views/accountformbase", "models/account","validation"],

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
                            checkEmail : true,
                            email: true
                        }
                    };
                },

                __submit : function(){
                    Account.login($(this.el));
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
