define(["jquery","views/signupform"], function($,SignupForm){

	return {
		run : function(){
			$(document).ready(function(){
                var signupForm = $(".signup-form");

                if(signupForm.length !== 0){
                    var form = SignupForm.get(signupForm);
                    form.on("invalid", function(field,message){
                        alert(message);
                    });
                }
			});
		}
	};

});
