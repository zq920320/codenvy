define(["jquery",
        "views/signupform",
        "views/signinform",
        "views/forgotpasswordform",
        "views/resetpasswordform",
        "views/errorreport"],

    function($,
        SignupForm,
        SigninForm,
        ForgotPasswordForm,
        ResetPasswordForm,
        ErrorReport){


        return {
            run : function(){
                $(document).ready(function(){
                    var signupForm = $(".signup-form"),
                        signinForm = $(".login-form"),
                        forgotPasswordForm = $(".forgotpassword-form"),
                        resetPasswordForm = $(".resetpassword-form"),
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

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                        })();
                    }

                    if(forgotPasswordForm.length !== 0){
                        (function(){
                            var form = ForgotPasswordForm.get(forgotPasswordForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("success", function(d){
                                alert(d.message);
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });

                        })();
                    }

                    if(resetPasswordForm.length !== 0){
                        (function(){

                            var form = ResetPasswordForm.get(resetPasswordForm),
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

                });
            }
        };

    }
);
