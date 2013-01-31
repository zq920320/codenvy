define(["jquery","underscore","backbone"],function($,_,Backbone){

	var SignupForm = Backbone.View.extend({
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