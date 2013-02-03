define(["jquery","views/signupform","views/errorreport"], function($,SignupForm,ErrorReport){

	return {
		run : function(){
			$(document).ready(function(){
                var signupForm = $(".signup-form"),
                    errorContainer = $(".error-container");

                if(signupForm.length !== 0){
                    var form = SignupForm.get(signupForm),
                        errorReport = ErrorReport.get(errorContainer);

                    form.on("invalid", function(field,message){

                        errorReport.show(message);

                        //alert(message);
                    });
                }
			});
		}
	};

});
