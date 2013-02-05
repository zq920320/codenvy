define(["backbone","views/accountformbase", "models/account"],

    function(Backbone,AccountFormBase,Account){

        var SignInForm = AccountFormBase.extend({

            initialize : function(arguments){
                AccountFormBase.prototype.initialize.apply(this,arguments);
            },

            __validationRules : function(){
                var rules = AccountFormBase.prototype.__validationRules.apply(this);
                rules.password = { required : true };
                return rules;
            },

            __submit : function(form){
                Account.login(
                    this.$("input[name='email']").val(),
                    this.$("input[name='password']").val(),
                    this.$("input[name='domain']").val(),
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
