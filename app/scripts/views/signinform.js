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
                    var loginUrl = "/sso/server/gen?authType=jaas";
                    $(this.el).attr("action", loginUrl);
                    $(this.el).submit();
/*                    Account.login(
                        $(this.el), // validation form fields
                        _.bind(function(){
                            //$(this.el).attr('action',data.url);
                            //$(this.el).submit();
                            //window.location = data.url;
                            return true;
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

                    return false;*/
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
