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
 
(function(window){

    var _gaq = _gaq || [];

    define(["jquery","json", "models/tenant","models/profile","cookies"],function($,JSON,Tenant,Profile){

        /*
            AccountError is used to report errors through error callback function
            (see details below ). Example usage:

            new AccountError("password","Your password is too short")

        */
        var userProfile = userProfile || {}; // user Profile to store user's data from server

        var showSupportLink = function(isPaid){
            if (isPaid){
                var uv = document.createElement('script'); uv.type = 'text/javascript'; uv.async = true;
                uv.src = ('https:' === document.location.protocol ? 'https://' : 'http://') + 'widget.uservoice.com/wfZmoiHoOptcKkBgu238zw.js';
                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(uv, s);
            }else {
                var el = $("footer").find("ul");
                el.append('<li><a class="footer-link" href="http://helpdesk.codenvy.com">Feedback & support<b></b></a></li>');
            }
        };
        var AccountError = function(fieldName, errorDescription){
            return {
                getFieldName : function(){
                    return fieldName;
                },

                getErrorDescription : function(){
                    return errorDescription;
                }
            };
        };

        var isBadGateway = function(jqXHR){
                return jqXHR.status === 502;
        };

        var getQueryParameterByName = function(name){
                name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
                var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
                var results = regex.exec(window.location.search);
                if(results){
                    return decodeURIComponent(results[1].replace(/\+/g, " "));
            }
        };

        /*

            Password Setup Id Provider is used to provide ids
            for confirmSetupPassword and setupPassword functions

        */

        /*

            Filling the Profile page
        */
        function onReceiveUserProfileInfo(response)
        {
                userProfile = response.attributes;
                var profileAttributes = userProfile.attributes;
                var attributes = {};
                profileAttributes.forEach(
                    function(attribute){
                        Object.defineProperty(attributes, attribute.name,{value:attribute.value});
                });
                document.getElementById("account_value").innerHTML = attributes.email || "";
                document.getElementsByName("first_name")[0].value = attributes.firstName || "";
                document.getElementsByName("last_name")[0].value = attributes.lastName || "";
                document.getElementsByName("phone_work")[0].value = attributes.phone || "";
                document.getElementsByName("company")[0].value = attributes.employer || "";
                document.getElementsByName("title")[0].value = attributes.jobtitle || "";
        }


        var loginWithGoogle = function(page,callback){
            if (isWebsocketEnabled()) {
               _gaq.push(['_trackEvent', 'Regisration', 'Google registration', page]);
                var url = "/api/oauth/authenticate?oauth_provider=google&mode=federated_login" +
                   "&scope=https://www.googleapis.com/auth/userinfo.profile&scope=https://www.googleapis.com/auth/userinfo.email"+
                   "&redirect_after_login=" + encodeURIComponent("/api/oauth?" + window.location.search.substring(1) + "&oauth_provider=google");

                if(typeof callback !== 'undefined'){
                    callback(url);
                }
            }
        };

        var loginWithGithub = function(page,callback){
            if (isWebsocketEnabled()) {
                _gaq.push(['_trackEvent', 'Regisration', 'GitHub registration', page]);
                var url = "/api/oauth/authenticate?oauth_provider=github&mode=federated_login&scope=user&scope=repo" +
                "&redirect_after_login=" + encodeURIComponent("/api/oauth?" + window.location.search.substring(1) + "&oauth_provider=github");

                if(typeof callback !== 'undefined'){
                    callback(url);
                }
            }
        };

        /*
            Every method accepts 0 or more data values and two callbacks (success and error)

            Success callback spec:

                function success(data){
                    // data includes any additonal data you want to pass back
                }

            Error callback spec:

                function error(errors){
                    // errors is a list of AccountError instances
                }

        */
        var isWebsocketEnabled = function(){
            var USER_AGENT = navigator.userAgent.toLowerCase();
            var IS_WS_SUPPORTED = ("WebSocket" in window);
            if (!IS_WS_SUPPORTED || (USER_AGENT.indexOf("chrome") === -1 && USER_AGENT.indexOf("firefox") === -1 && USER_AGENT.indexOf("safari") === -1)) {
                window.location = "/site/error/browser-not-supported#show";
                return false;
            } 
            return true;
        };

        var removeCookie = function(cookie){
            if ($.cookie(cookie)){
                $.cookie(cookie, null);
            }
        };

        return {

            removeCookie : removeCookie,

            isWebsocketEnabled :isWebsocketEnabled,

            getQueryParameterByName : getQueryParameterByName,

            AccountError : AccountError,

            isValidDomain : function(domain){

                return (/^[a-z0-9][a-z0-9_.-]{2,19}$/).exec(domain) !== null ;
            },

            isValidEmail : function(email){
                return (/^[^\+\/]+$/).test(email);
            },

            login : function(email, password, redirect_url, success, error){

                if (isWebsocketEnabled()){
                    var loginUrl = "/api/auth/login?" + window.location.search.substring(1);
                    var selectWsUrl = "../site/private/select-tenant?cookiePresent&" + window.location.search.substring(1);
                    var data = {username: email, password: password};
                 $.ajax({
                    url : loginUrl,
                    type : "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success : function(){
                        if (redirect_url) {
                            success({url: redirect_url});
                        } else {
                            success({url: selectWsUrl});
                        }
                    },
                    error : function(xhr/*, status , err*/){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });             
                    }
                },

            adminLogin : function(email, password, redirect_url, success, error){

                if (isWebsocketEnabled()){
                    var loginUrl = "/api/auth/login?" + window.location.search.substring(1);
                    var selectWsUrl = "../site/private/select-tenant?cookiePresent&" + window.location.search.substring(1);
                    var data = {username: email, password: password, realm:"sysldap"};
                 $.ajax({
                    url : loginUrl,
                    type : "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success : function(){
                        if (redirect_url) {
                            success({url: redirect_url});
                        } else {
                            success({url: selectWsUrl});
                        }
                    },
                    error : function(xhr/*, status , err*/){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });             
                    }
                },                

            loginWithGoogle : loginWithGoogle,
            loginWithGithub : loginWithGithub,

            createTenant : function(email,domain,success,error){
                var data = {email: email.toLowerCase(), workspacename: domain.toLowerCase()};
                var emailValidateUrl = "/api/internal/token/validate?" + window.location.search.substring(1);
                $.ajax({
                    url : emailValidateUrl,
                    type : "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success : function(){
                        success({url: '../site/thank-you'});
                    },
                    error : function(xhr/*, status , err*/){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            },

            createWorkspace : function(username,bearertoken,workspace,redirect_url,success,error){
                var data = {username: username.toLowerCase(), token: bearertoken};
                var destinationUrl = window.location.protocol + "//" + window.location.host + "/ide/" + workspace + "?" +
                    window.location.search.substring(1);
                var waitUrl = "../wait-for-tenant?type=create&tenantName=" + workspace + "&redirect_url=" + encodeURIComponent(destinationUrl);
                var workspaceName = {name: workspace};
                var authenticateUrl = "/api/internal/token/authenticate";
                var createWSUrl = "/api/workspace/create";
                var selectWsUrl = "/site/private/select-tenant";
                $.ajax({
                    url : authenticateUrl,
                    type : "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success : function(){
                        if (workspace){
                            $.ajax({
                                url : createWSUrl,
                                type : "POST",
                                contentType: "application/json",
                                data: JSON.stringify(workspaceName),
                                success : function(){
                                    success({url: waitUrl});
                                },
                                error : function(xhr/*, status , err*/){
                                    error([
                                        new AccountError(null,xhr.responseText)
                                    ]);
                                }
                            });
                        } else {
                            if (redirect_url) {
                                success({url: redirect_url});
                            } else {
                                success({url: selectWsUrl});
                            }
                        }
                    },
                    error : function(xhr/*, status , err*/){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            },

            joinWorkspace : function(username,bearertoken,workspace,success,error){
                var data = {username: username.toLowerCase(), token: bearertoken};
                var destinationUrl = window.location.protocol + "//" + window.location.host + "/ide/" + workspace;
                var waitUrl = "../wait-for-tenant?type=start&tenantName=" + workspace + "&redirect_url=" + encodeURIComponent(destinationUrl);
                //var workspaceName = {name: workspace};
                var authenticateUrl = "/api/internal/token/authenticate";
                $.ajax({
                    url : authenticateUrl,
                    type : "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success : function(){
                        success({url: waitUrl});
                    },
                    error : function(xhr/*, status , err*/){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            },

            recoverPassword : function(email,success,error){
                //implementation based on this:
                //https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/recover-password.js

                var passwordRecoveryUrl = "/api/password/recover/" + email;

                $.ajax({
                    url : passwordRecoveryUrl,
                    type : "POST",
                    data: {},
                    success : function(output, status, xhr){
                        success({message: xhr.responseText});
                    },
                    error : function(xhr){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            },

            confirmSetupPassword : function(success,error){
                // implementation based on this:
                // https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/setup-password.js
                // just like with setupPassword, we expect the id to be in the url:
                // https://codenvy.com/pages/setup-password?id=df3c62fe-1459-48af-a4a0-d0c1cc17614a

                var confirmSetupPasswordUrl = "/api/password/verify",
                    id = getQueryParameterByName("id");

                if(typeof id === 'undefined'){
                    error([
                        new AccountError(null,"Invalid password reset url")
                    ]);

                    return;
                }

                $.ajax({
                    url : confirmSetupPasswordUrl + "/" + id,
                    type : "GET",
                    success : function(output, status, xhr){
                        success({ email : xhr.responseText });
                    },
                    error : function(xhr /*,status , err*/){
                        setTimeout(function(){window.location = "/site/recover-password";}, 10000);
                        error([
                            new AccountError(null,xhr.responseText + ".<br>You will be redirected in 10 sec")
                        ]);
                    }
                });

            },

            setupPassword : function(password,success,error){
                // implementation based on this:
                // https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/setup-password.js
                // We assume that uid is part of the url :
                //  https://codenvy.com/pages/setup-password?id=df3c62fe-1459-48af-a4a0-d0c1cc17614a

                var setupPasswordUrl = "/api/password/setup",
                    id = getQueryParameterByName("id");


                $.ajax({
                    url : setupPasswordUrl,
                    type : "POST",
                    data : { uuid : id, password : password },
                    success : function(){
                        success({url: "/site/login"});
                    },
                    error : function(xhr){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            },
            // change password in Profile page
            changePassword : function(password,success,error){

                var changePasswordUrl = "/api/user/password";

                $.ajax({
                    url : changePasswordUrl,
                    type : "POST",
                    data : "password="+password,
                    success : function(){
                        success({url: "/"});
                    },
                    error : function(xhr){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            },
            // update User`s profile in Profile page
            updateProfile : function(userAttributes,success,error){
                
                // userProfile.attributes = body;//Updating profile attributes
                Object.getOwnPropertyNames(userAttributes).forEach(function(prop){
                    var newAttribute = true;
                    userProfile.attributes.forEach(function(attribute){
                        if (attribute.name === prop) {
                            attribute.value = userAttributes[prop];
                            newAttribute = false;
                        }
                    });
                    if (newAttribute){
                        var el = {};
                        el["name"] = prop;
                        el["value"] = userAttributes[prop];
                        userProfile.attributes.push(el);
                    }

                });
                var data = JSON.stringify(userProfile.attributes);
                $.ajax({
                    url : "/api/profile",
                    type : "POST",
                    data : data,
                    contentType: "application/json; charset=utf-8",
                    success : function(){
                        success();
                    },
                    error : function(xhr){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });

            },

            // get User`s profile in Profile page
            getUserProfile : function(success,error){
                $.when(Profile.getUser()).done(function(user){
                   onReceiveUserProfileInfo(user);
                }).fail(function(msg){
                    error([
                        new  AccountError(null,msg)
                    ]);
                });
            },

            /**
             * Encode all special characters including ~!*()'. Replace " " on "+"
             * @see http://xkr.us/articles/javascript/encode-compare/
             * @param {Object} string
             */
            encodeSpecialSymbolsForPost: function (string)
            {
               if (string)
               {
                  string = encodeURIComponent(string);
                  string = string.replace(/~/g, escape("~"));
                  string = string.replace(/!/g, escape("!"));
                  string = string.replace(/\*/g, escape("*"));
                  string = string.replace(/[(]/g, escape("("));
                  string = string.replace(/[)]/g, escape(")"));
                  string = string.replace(/'/g, escape("'"));
                  string = string.replace(/%20/g, escape("+"));
               }

               return string;
            },

          /**
           * Escape special symbols from user input
           * @param string
           * @returns
           */
          escapeSpecialSymbols : function (string)
          {
            if (string)
                {
                string = string.replace(/\n/g, "\\n");
                string = string.replace(/\r/g, "\\r");
                string = string.replace(/\t/g, "\\t");
                string = string.replace(/[\b]/g, "\\b");
                string = string.replace(/\f/g, "\\f");
                string = string.replace(/\\/g, "\\\\");
                string = string.replace(/\"/g, "\\\"");
                }
            return string;
            },

            getTenants : function(success,error,redirect){
                $.when(Tenant.getTenants()).done(function(tenants){
                    switch (tenants.length) {
                        case 0: redirect({url:"/site/create-account"});
                            break;
                        case 1: redirect({url:"/ide/" + tenants[0].toJSON().name});
                            break;
                        default: 
                            $.when(Profile.getUser()).done(function(user){
                                success(tenants,user);
                            }).fail(function(msg){
                                error([
                                    new  AccountError(null,msg)
                                ]);
                            });
                }
                }).fail(function(msg){
                    error([
                        new  AccountError(null,msg)
                    ]);
                });
            },

            // Returns true if User has WS with tariff plan
            supportTab : function(){
                var getAccountUrl = "/api/organization/subscriptions";
                var paid = false;
                $.ajax({
                    url : getAccountUrl,
                    type : "GET",
                    async : false,
                    success : function(subscriptions){
                        if (typeof(subscriptions)==='object'){
                            subscriptions.forEach(
                                function(subscription){
                                        if (subscription.serviceId){
                                            paid = true;
                                        }
                            });
                                
                        }
                    },
                    error : function(){
                    }
                });
            showSupportLink(paid);
            },

            // Changing login page behavior if authtype=ldap
            isAuthtypeLdap : function() {
                var type = getQueryParameterByName("authtype");
                return type;

            },

            waitForTenant : function(success, error){
                //based on : https://github.com/codenvy/cloud-ide/blob/8fe1e50cc6434899dfdfd7b2e85c82008a39a880/cloud-ide-war/src/main/webapp/js/wait-tenant-creation.js
                var type = getQueryParameterByName("type");//create OR start
                var redirectUrl = getQueryParameterByName("redirect_url");
                var tenantName = getQueryParameterByName("tenantName");
                if(typeof tenantName === 'undefined'){
                    error([
                        new AccountError(null,"This is not a valid url")
                    ]);
                }

                var MAX_WAIT_TIME_SECONDS = 180,
                    PING_TIMEOUT_MILLISECONDS = 500,
                    endTime = new Date().getTime() + MAX_WAIT_TIME_SECONDS * 1000;

                function buildRedirectUrl(){ return redirectUrl; }


                function hitServer(){

                    if(new Date().getTime() >= endTime){
                    // removing autologin cookie if exist
                    removeCookie("autologin");
                        if (type === "create"){
                            error([
                                new AccountError(
                                    null,
                                    "Workspace creation delayed. We'll email you the credentials after your workspace is created."
                                )
                            ]);
                        } else if (type === "factory"){
                            window.location = "/site/error/error-factory-creation";              
                        }else{
                            error([
                                new AccountError(
                                    null,
                                    "The requested workspace <strong>'" + tenantName + "'</strong> is not available. Please, contact support."
                                )

                            ]);
                        }

                        return;
                    }

                    $.ajax({
                        url : "/cloud-admin/rest/cloud-admin/tenant-service/tenant-state/" + tenantName,
                        type : "GET",
                        success : function(output,status, xhr){
                            if(xhr.responseText === "ONLINE"){
                                success({
                                    url : buildRedirectUrl()
                                });
                            } else if(xhr.responseText === "CREATION_FAIL"){
                                success({
                                    url : "/site/error/error-create-tenant"
                                });
                            }else{
                                setTimeout(hitServer,PING_TIMEOUT_MILLISECONDS);
                            }
                        },
                        error : function(xhr){
                            if(isBadGateway(xhr)){
                                error([
                                    new AccountError(null,"The requested workspace is not available. Please, contact support.")
                                ]);
                            } else {
                                error([
                                    new AccountError(null,xhr.responseText)
                                ]);
                            }
                        }
                    });
                }

                hitServer();

            }

        };
    });
}(window));
