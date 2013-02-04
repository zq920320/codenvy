define(["backbone","views/"], function(Backbone){

	var ForgotPasswordForm = Backbone.View.extend({

	});

	return {

		get : function(form){
			if(typeof form === 'undefined'){
				throw new Error("Need a form");
			}

			return new ForgotPasswordForm({ el : form });
		},

		ForgotPasswordForm : ForgotPasswordForm

	};

});