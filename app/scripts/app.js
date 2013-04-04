define(["jquery","config","marketo",
        "views/signupform",
        "views/signinform",
        "views/forgotpasswordform",
        "views/resetpasswordform",
        "views/errorreport",
        "views/successreport",
        "views/selectdomain",
        "views/ideloader",
        "views/contactform",
        "views/setprofilepasswordform",
        "views/setprofileuserform",
        "views/inviteorganization",
        "views/errorresponse",
        "views/maintenancepage"
        ],

    function($,Config,Munchkin,
        SignupForm,
        SigninForm,
        ForgotPasswordForm,
        ResetPasswordForm,
        ErrorReport,
        SuccessReport,
        SelectDomain,
        IDELoader,
        ContactForm,
        SetProfilePassword,
        SetProfileUser,
        InviteOrganization,
        ErrorResponse,
        MaintenancePage){

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

                    Munchkin.init('577-PCT-880');

                    var signupForm = $(".signup-form"),
                        signinForm = $(".login-form"),
                        forgotPasswordForm = $(".forgotpassword-form"),
                        resetPasswordForm = $(".resetpassword-form"),
                        errorContainer = $(".error-container"),
                        domainSelector = $(".select-domain"),
                        loader = $(".loader"),
                        contactForm = $(".contact-form"),
                        setProfilePassword = $(".change-password-form"),
                        setProfileUserForm = $(".cloud-ide-profile"),
                        inviteOrganization = $(".organization"),
                        errorResponse = $(".ErrorIcon"),
                        maintenancePage = $(".maintenance");

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
                        }());
                    }

                    if(signinForm.length !== 0){
                        (function(){
                            var form = SigninForm.get(signinForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            // form.on("success", function(){
                            //     alert("logged in");
                            // });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                        }());
                    }

                    if(forgotPasswordForm.length !== 0){
                        (function(){
                            var form = ForgotPasswordForm.get(forgotPasswordForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });


                        }());
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

                        }());
                    }

                    if(domainSelector.length !== 0){
                        new SelectDomain.get(domainSelector);
                    }

                    if(loader.length !== 0){
                        var ideloader = new IDELoader.IDELoader(),
                            errorReport = ErrorReport.get(errorContainer);

                        ideloader.on("ready",function(d){
                            window.location.href = d.url;
                        });

                        ideloader.on("error",function(message){
                            errorReport.show(message);
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

                        }());
                    }

                    //setProfileUser
                    if(setProfileUserForm.length !== 0){
                        (function(){

                            var form = SetProfileUser.get(setProfileUserForm),
                                errorReport = ErrorReport.get(errorContainer),
                                successReport = SuccessReport.get(errorContainer);
                            form.getUserProfileInfo(function(field,message){
                                errorReport.show(message);
                                });
                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("success", function(){
                                successReport.show(form.settings.successUpdatingProfile);

                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);

                            });

                        }());
                    }

                    if(setProfilePassword.length !== 0){
                        (function(){

                            var form = SetProfilePassword.get(setProfilePassword),
                                errorReport = ErrorReport.get(errorContainer),
                                successReport = SuccessReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("success", function(){
                                successReport.show(form.settings.successChangingPassword);
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });

                        }());
                    }

                    // success invite organization
                    if(inviteOrganization.length !== 0){
                        (function(){
                            var element = InviteOrganization.get(inviteOrganization);
                            element.setOrganization();
                        }());
                    }

                    // put error to page from response
                    if(errorResponse.length !== 0){
                        (function(){
                            var element = ErrorResponse.get(errorResponse);
                            element.setError();
                        }());
                    }
                    
                    // maintenance page. queue number
                    if(maintenancePage.length !== 0){
                        (function(){
                            MaintenancePage.get(maintenancePage);
                            
                            
                        }());
                    }

                });
            }
        };

    }
);
