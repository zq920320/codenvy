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
        "views/signinform",
        "views/forgotpasswordform",
        "views/resetpasswordform",
        "views/errorreport",
        "views/ws_createform",
        "views/adminform",
        "views/create_ws_add_member",
        "views/onpremises-login",
        "views/factory-usage-notification"
        ],

    function($,Config,
        SigninForm,
        ForgotPasswordForm,
        ResetPasswordForm,
        ErrorReport,
        WSCreateForm,
        AdminForm,
        CreateWsAdd_Member,
        OnPremisesLogin,
        FactoryUsageNotification){

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
                    var uvOptions = {}; //UserVoice object
                    
                    if (uvOptions){}
                    var signinForm = $(".login-form"),
                        forgotPasswordForm = $(".forgotpassword-form"),
                        resetPasswordForm = $(".resetpassword-form"),
                        errorContainer = $(".error-container"),
                        wsCreateForm = $(".create-form"),
                        adminForm = $(".admin-form"),
                        creatWsAddMember = $(".create-ws-add-memeber"),
                        onpremloginForm = $(".onpremloginForm"),
                        factoryUsageNotification =  $(".factory-notification");

                    if(factoryUsageNotification.length !== 0){
                        (function(){
                            FactoryUsageNotification.get(factoryUsageNotification);
                        }());
                        
                    }

                    if(onpremloginForm.length !== 0){
                        (function(){
                            var form = OnPremisesLogin.get(onpremloginForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
                            });

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                            
                        }());
                    }
                    
                    if(creatWsAddMember.length !== 0){
                        (function(){
                            var form = CreateWsAdd_Member.get(creatWsAddMember),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                            
                        }());
                    }

                    if(wsCreateForm.length !== 0){
                        (function(){
                            var form = WSCreateForm.get(wsCreateForm),
                            errorReport = ErrorReport.get(errorContainer);

                            form.on("submitting", function(){
                                errorReport.hide();
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

                            form.on("invalid", function(field,message){
                                errorReport.show(message);
                            });
                        }());
                    }

                    if(adminForm.length !== 0){
                        (function(){
                            var form = AdminForm.get(adminForm),
                            errorReport = ErrorReport.get(errorContainer);
                            if ($(".error-container").html()){
                                $(".error-container").addClass("expanded");
                            }

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
                    
                });
            }
        };

    }
);
