define(["jquery","underscore","backbone","validation"],function($,_,Backbone){


    var ProgressiveForm = Backbone.View.extend({
        initialize : function(){

            console.log("calling progressive form");

            this.$(".field > input").on("focus", _.bind(function(){
                this.adjustFormDisplay();
            },this));

            this.$(".field > input").on("blur", _.bind(function(){
                this.adjustFormDisplay();
            },this));
        },

        adjustFormDisplay : function(){
            if(this.$(".field > input").toArray().indexOf(document.activeElement) !== -1){
                this.$(".field > .signup-button").removeClass("hidden-button");
                this.$(".field > .domain-name").removeClass("hidden-text-box");
                $(".aternative-login").addClass("collapsed");
            } else {
                this.$(".field > .signup-button").addClass("hidden-button");
                this.$(".field > .domain-name").addClass("hidden-text-box");
                $(".aternative-login").removeClass("collapsed");
            }
        }
    });





	var SignupForm = ProgressiveForm.extend({
        initialize : function(arguments){

            ProgressiveForm.prototype.initialize.apply(this,arguments);

            $(this.el).validate({
                submitHandler: function(form) {
                    alert("submitting");
                    //form.submit();
                }
            });
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

});
