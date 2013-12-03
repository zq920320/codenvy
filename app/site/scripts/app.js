/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
 
define(["jquery","config",
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
        "views/maintenancepage",
        "views/gc_banner",
        "views/ws_createform",
        "views/create_ws"
        ],

    function($,Config,
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
        MaintenancePage,
        GC_banner,
        WSCreateForm,
        CreatingWorkspace){

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
                        contactForm = $(".contact-form"),
                        setProfilePassword = $(".change-password-form"),
                        setProfileUserForm = $(".cloud-ide-profile"),
                        inviteOrganization = $(".organization"),
                        errorResponse = $(".ErrorIcon"),
                        maintenancePage = $(".maintenance"),
                        gcBannerElement = $(".cta"),
                        wsCreateForm = $(".create-form"),
                        creatingWorkspace = $(".creating-ws");


                    if(gcBannerElement.length !== 0){
                        (function(){
                            // <!--  CTA Banner Rotation   -->
                            new GC_banner();
                        }());
                    }

                    if(gcBannerElement.length !== 0){
                        (function(){
                            // <!--  CTA Banner Rotation   -->
                            new GC_banner();
                        }());
                    }

                    if(wsCreateForm.length !== 0){
                        (function(){
                            var form = WSCreateForm.get(wsCreateForm),
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

                    if(creatingWorkspace.length !== 0){
                        (function(){
                            var page = CreatingWorkspace.get(creatingWorkspace),
                            errorReport = ErrorReport.get(errorContainer);

                            page.on("success", function(d){
                                window.location.href = d.url;
                            });

                            page.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                            
                        }());
                    }

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
                            if ($(".error-container").html()){
                                $(".error-container").addClass("expanded");
                            }
                            form.on("success", function(d){
                                window.location.href = d.url;
                            });

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
                            ideloader.setLoaderMessage();
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
