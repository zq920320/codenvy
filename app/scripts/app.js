define(["jquery",
        "config",
        "views/signupform",
        "views/signinform",
        "views/forgotpasswordform",
        "views/resetpasswordform",
        "views/errorreport",
        "views/selectdomain",
        "views/ideloader",
        "views/contactform"
        ],

    function($,
        Config,
        SignupForm,
        SigninForm,
        ForgotPasswordForm,
        ResetPasswordForm,
        ErrorReport,
        SelectDomain,
        IDELoader,
        ContactForm){


        function modernize(){
            Modernizr.load({
                // HTML5 placeholder for input elements
                test : Modernizr.input.placeholder,
                nope : Config.placeholderPolyfillUrl,
                complete : function(){
                    if(typeof $.fn.placeholder !== 'undefined'){
                        $('input, textarea').placeholder();
                    }
                }
            });
        }

        return {
            run : function(){
                $(document).ready(function(){

                    modernize();


                    var signupForm = $(".signup-form"),
                        signinForm = $(".login-form"),
                        forgotPasswordForm = $(".forgotpassword-form"),
                        resetPasswordForm = $(".resetpassword-form"),
                        errorContainer = $(".error-container"),
                        domainSelector = $(".select-domain"),
                        loader = $(".loader"),
                        contactForm = $(".contact-form");

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
                                form.showMessage(d.message);
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

                            form.resolveUserEmail();

                        })();
                    }

                    if(domainSelector.length !== 0){
                        var selectdomain = new SelectDomain.get(domainSelector);
                    }

                    if(loader.length !== 0){
                        new IDELoader.IDELoader().on("ready",function(d){
                            window.location.href = d.url;
                        });
                    }

                    if(contactForm.length !== 0){
                        (function(){
                            var form = ContactForm.get(contactForm),
                                errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
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
