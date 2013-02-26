define(["jquery","underscore","models/account","views/accountformbase","validation"],

    function($,_,Account,AccountFormBase){

        var ProgressiveForm = AccountFormBase.extend({
            initialize : function(attributes){

                AccountFormBase.prototype.initialize.apply(this,attributes);

                this.canCollapse = true;

                this.$(".field > input").on("focus", _.bind(function(){
                    this.adjustFormDisplay();
                },this));

                this.$(".field > input").on("blur", _.bind(function(){
                    this.adjustFormDisplay();
                },this));

                this.$("input[type='submit']").click(_.bind(function(){
                    this.stopFromCollapsing();
                },this));
            },

            adjustFormDisplay : function(){

                //if all text fields are filled out, never collapse
                var textBoxes = this.$(".field > input").toArray(), empty = false;

                _.each(textBoxes,function(tb){
                    if($(tb).val() === ''){
                        empty = true;
                    }
                });

                if(!empty){
                    this.stopFromCollapsing();
                }

                if(this.$(".field > input").toArray().indexOf(document.activeElement) !== -1){
                    this.$(".field > .signup-button").removeClass("hidden-button");
                    this.$(".field > .domain-name").parent().removeClass("hidden-text-box");
                    $(".aternative-login").addClass("collapsed");
                } else {
                    if(this.canCollapse){
                        this.$(".field > .signup-button").addClass("hidden-button");
                        this.$(".field > .domain-name").parent().addClass("hidden-text-box");
                        $(".aternative-login").removeClass("collapsed");
                    }
                }
            },

            stopFromCollapsing : function(){
                this.canCollapse = false;
            }
        });


        var SignupForm = ProgressiveForm.extend({

            initialize : function(attributes){
                ProgressiveForm.prototype.initialize.apply(this,attributes);
            },

            __submit : function(form){
                this.__showProgress();

                this.stopFromCollapsing();

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

                return false;
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
