define(["jquery", "underscore", "backbone", "validation"],

    function($,_,Backbone){

        var Form = Backbone.View.extend({
            initialize : function(){
/*                $(this.el).on('submit', function(e){
                    e.preventDefault();
                });*/

                this.validator = $(this.el).validate({
                    rules: this.__validationRules(),
                    messages: this.__validationMessages(),
                    onfocusout : false, onkeyup : false,
                    submitHandler: _.bind(this.__submit,this),
                    showErrors : _.bind(function(errorMap, errorList){
                        this.__showErrors(errorMap, errorList);
                    },this)
                });
            },

            __validationRules : function(){
                throw new Error("Not implemented");
            },

            __validationMessages : function(){
                throw new Error("Not implemented");
            },

            __showErrors : function(){
                throw new Error("Not implemented");
            },

            __restoreForm : function(){
                throw new Error("Not implemented");
            },

            __showProgress : function(){
                throw new Error("Not implemented");
            }
        });

        return Form;
    }
);
