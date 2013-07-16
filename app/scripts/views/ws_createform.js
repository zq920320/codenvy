(function(window){
    define(["jquery", "underscore", "views/accountformbase", "models/account","validation"],

        function($,_,AccountFormBase,Account){

            var WSCreateForm = AccountFormBase.extend({

                initialize : function(attributes){
                    AccountFormBase.prototype.initialize.apply(this,attributes);

                    //bind onclick to Google and GitHub buttons

                    $(".oauth-button.google").click(function(){
                        Account.loginWithGoogle("Create WS page", function(url){
                            window.location = url;
                        });
                    });

                    $(".oauth-button.github").click(function(){
                        Account.loginWithGithub("Create WS page", function(url){
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

            __submit : function(form){
                if (Account.isWebsocketEnabled()) {
               
                this.trigger("submitting");

                Account.createTenant(
                    $(form).find("input[name='email']").val(),
                    $(form).find("input[name='domain']").val(),
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
                return false;
            }

            });

            return {
                get : function(form){
                    if(typeof form === 'undefined'){
                        throw new Error("Need a form");
                    }

                    return new WSCreateForm({
                        el : form
                    });
                },

                WSCreateForm : WSCreateForm
            };
        }
    );
}(window));
