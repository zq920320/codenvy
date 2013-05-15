(function(window){

    var _gaq = _gaq || [];

    define(["jquery","json", "models/tenant","cookies"],function($,JSON,Tenant){

        /*
            AccountError is used to report errors through error callback function
            (see details below ). Example usage:

            new AccountError("password","Your password is too short")

        */

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


        function onReceiveUserProfileInfo(request)
        {
                var user = JSON.parse(request.responseText);
                document.getElementsByName("first_name")[0].value = user.firstName || "";
                document.getElementsByName("last_name")[0].value = user.lastName || "";
                document.getElementsByName("phone_work")[0].value = user.phone || "";
                document.getElementsByName("company")[0].value = user.employer || "";
                document.getElementsByName("title")[0].value = user.jobtitle || "";
        }


        var loginWithGoogle = function(page,callback){
            if (isWebsocketEnabled()) {
               _gaq.push(['_trackEvent', 'Regisration', 'Google registration', page]);
                var url = "/sso/server/gen?authType=oauth&oauth_provider=google" +
                   "&scope=https://www.googleapis.com/auth/userinfo.profile&scope=https://www.googleapis.com/auth/userinfo.email";
                //window.location = url;
                if(typeof callback !== 'undefined'){
                    callback(url);
                }
            }
        };

        var loginWithGithub = function(page,callback){
            if (isWebsocketEnabled()) {
                _gaq.push(['_trackEvent', 'Regisration', 'GitHub registration', page]);
                var url = "/sso/server/gen?authType=oauth&oauth_provider=github&scope=user&scope=repo";
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
                window.location = "/error/browser-not-supported#show";
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

                //check for the trailing dash
                var d = domain || "", ds = d.split(".codenvy.com");

                if(ds.length === 2){

                    var name = ds[0];

                    if(name[name.length-1] === '-'){
                        return false;
                    }
                }

                return (
                    /^[A-Za-z]{1}[A-Za-z0-9\-]{0,19}\.codenvy\.com/g
                ).exec(domain) !== null ;
            },

            isValidEmail : function(email){
                return (/^[A-Za-z]{1}[a-zA-Z0-9@_\.-]+$/).test(email);
            },

            login : function(form){

                if (isWebsocketEnabled()){
                    var loginUrl = "/sso/server/gen?authType=jaas";
                    if(getQueryParameterByName("redirect_url") && getQueryParameterByName("client_url"))
                    {
                        loginUrl+="&redirect_url="+getQueryParameterByName("redirect_url");
                        loginUrl+="&client_url="   +getQueryParameterByName("client_url");
                    }

                    form.attr("action", loginUrl);
                    form.submit();
                    }
                },

            loginWithGoogle : loginWithGoogle,
            loginWithGithub : loginWithGithub,

            createTenant : function(email,domain,success,error){

                var tenantServiceUrl = "/cloud-admin/rest/cloud-admin/public-tenant-service/create-with-confirm/";

                $.ajax({
                    url : tenantServiceUrl + encodeURIComponent(domain.toLowerCase()) + "/" + encodeURIComponent(email.toLowerCase()),
                    type : "POST",
                    data: {},
                    success : function(){
                        success({url: '../thank-you'});
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

                var passwordRecoveryUrl = "/rest/password/recover/" + email;

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

                var confirmSetupPasswordUrl = "/rest/password/verify",
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
                        setTimeout(function(){window.location = "/recover-password";}, 10000);
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

                var setupPasswordUrl = "/rest/password/setup",
                    id = getQueryParameterByName("id");


                $.ajax({
                    url : setupPasswordUrl,
                    type : "POST",
                    data : { uuid : id, password : password },
                    success : function(){
                        success({url: "/login"});
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

                var changePasswordUrl = "/rest/private/password/change",
                    id = getQueryParameterByName("id");

                $.ajax({
                    url : changePasswordUrl,
                    type : "POST",
                    data : { uuid : id, password : password },
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
            updateProfile : function(body,success,error){

                var profileUrl = "/rest/private/profile/current";

                $.ajax({
                    url : profileUrl,
                    type : "POST",
                    data : body,
                    contentType: "application/json",
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

            // get User`s profile in Profile page
            getUserProfile : function(error){
                var profileUrl = "/rest/private/profile/current";
                $.ajax({
                    url : profileUrl,
                    type : "GET",
                    contentType: "application/json",
                    success : function(output, status, xhr){
                        onReceiveUserProfileInfo(xhr);//filling profile page
                    },
                    error : function(xhr){
                        error(null,xhr.responseText);
                    }
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

            getTenants : function(success,error){
                $.when(Tenant.getTenants()).done(function(tenants){
                    success(tenants);
                }).fail(function(msg){
                    error([
                        new  AccountError(null,msg)
                    ]);
                });
            },


            waitForTenant : function(success, error){
                //based on : https://github.com/codenvy/cloud-ide/blob/8fe1e50cc6434899dfdfd7b2e85c82008a39a880/cloud-ide-war/src/main/webapp/js/wait-tenant-creation.js
                var errorType = getQueryParameterByName("errorType");//create OR start
                var redirectUrl = getQueryParameterByName("redirect_url");
                var tenantName = getQueryParameterByName("tenantName");
                if(typeof tenantName === 'undefined'){
                    error([
                        new AccountError(null,"This is not a valid url")
                    ]);
                }

                var MAX_WAIT_TIME_SECONDS = 120,
                    PING_TIMEOUT_MILLISECONDS = 500,
                    endTime = new Date().getTime() + MAX_WAIT_TIME_SECONDS * 1000;

                function buildRedirectUrl(){ return redirectUrl; }


                function hitServer(){

                    if(new Date().getTime() >= endTime){
                    // removing autologin cookie if exist
                    removeCookie("autologin");
                        if (errorType === "create"){
                            error([
                                new AccountError(
                                    null,
                                    "Tenant creation delayed, we will send credentials on your email when tenant started."
                                )
                            ]);
                        }else{
                            error([
                                new AccountError(
                                    null,
                                    "The requested tenant <strong>'" + tenantName + "'</strong> is not available. Please, contact support."
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
                            }else{
                                setTimeout(hitServer,PING_TIMEOUT_MILLISECONDS);
                            }
                        },
                        error : function(xhr){
                            if(isBadGateway(xhr)){
                                error([
                                    new AccountError(null,"The requested domain is not available. Please, contact support.")
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
