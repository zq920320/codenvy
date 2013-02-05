define(["jquery","views/signupform","views/signinform","views/errorreport"],

    function($,SignupForm,SigninForm,ErrorReport){


        return {
            run : function(){
                $(document).ready(function(){
                    var signupForm = $(".signup-form"),
                        signinForm = $(".login-form"),
                        errorContainer = $(".error-container");

                    if(signupForm.length !== 0){
                        (function(){
                            var form = SignupForm.get(signupForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("success", function(d){
                                window.location.href = d.url;
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                        })();
                    }

                    if(signinForm.length !== 0){
                        (function(){
                            var form = SigninForm.get(signinForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("success", function(){
                                alert("logged in");
                            });

                            /*jslint unparam: true*/
                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                            /*jslint unparam: false*/
                        })();
                    }
                });
            }
        };

    }
);
