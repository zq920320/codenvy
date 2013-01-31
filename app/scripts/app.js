define(["jquery","views/signupform"], function($,SignupForm){

	return {
		run : function(){
			$(document).ready(function(){
                var signupForm = $(".signup-form");

                if(signupForm.length !== 0){
                    SignupForm.get(signupForm);
                }
			});
		}
	};

});
