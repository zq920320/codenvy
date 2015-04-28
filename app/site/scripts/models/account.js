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
(function(window) {
    var _gaq = _gaq || [];
    define(["jquery", "json", "cookies"], function($, JSON) {
        /*
            AccountError is used to report errors through error callback function
            (see details below ). Example usage:

            new AccountError("password","Your password is too short")

        */

        var AccountError = function(fieldName, errorDescription) {
            return {
                getFieldName: function() {
                    return fieldName;
                },
                getErrorDescription: function() {
                    return errorDescription;
                }
            };
        };

        var getQueryParameterByName = function(name, queryString) {
            if (typeof queryString === 'undefined') {
                queryString = window.location.search;
            }
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
            var results = regex.exec(queryString);
            if (results) {
                return decodeURIComponent(results[1].replace(/\+/g, " "));
            }
        };
 
        var loginWithGoogle = function(page, callback) {
            if (isWebsocketEnabled()) {
                var redirectAfterLogin=encodeURIComponent(window.location.origin + "/api/oauth?" + window.location.search.substring(1) + (window.location.search ? '&' : '') + 'oauth_provider=google' + window.location.hash);
                _gaq.push(['_trackEvent', 'Regisration', 'Google registration', page]);
                var url = "/api/oauth/authenticate?oauth_provider=google&mode=federated_login" + "&scope=https://www.googleapis.com/auth/userinfo.profile&scope=https://www.googleapis.com/auth/userinfo.email" + "&redirect_after_login=" + redirectAfterLogin;
                if (typeof callback !== 'undefined') {
                    callback(url);
                }
            }
        };
        var loginWithGithub = function(page, callback) {
            if (isWebsocketEnabled()) {
                _gaq.push(['_trackEvent', 'Regisration', 'GitHub registration', page]);
                var redirectAfterLogin=encodeURIComponent(window.location.origin + "/api/oauth?" + window.location.search.substring(1) + (window.location.search ? '&' : '') + 'oauth_provider=github' + window.location.hash);
                var url = "/api/oauth/authenticate?oauth_provider=github&mode=federated_login&scope=user,repo,write:public_key" + "&redirect_after_login=" + redirectAfterLogin;
                if (typeof callback !== 'undefined') {
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
        var isWebsocketEnabled = function() {
            var USER_AGENT = navigator.userAgent.toLowerCase();
            if (USER_AGENT.indexOf("chrome") === -1 && USER_AGENT.indexOf("firefox") === -1 && USER_AGENT.indexOf("safari") === -1) {
                window.location = "/site/error/browser-not-supported#show";
                return false;
            }
            var IS_WS_SUPPORTED = ("WebSocket" in window);
            if (!IS_WS_SUPPORTED) {
                window.location = "/site/error/websocket-connection-error";
                return false;
            }
            return true;
        };
        var removeCookie = function(cookie) {
            if ($.cookie(cookie)) {
                $.removeCookie("cookie", {
                    path: "/"
                });
            }
        };
        var authenticate = function(username, bearertoken) {
            var deferredResult = $.Deferred();
            var data = {
                username: username.toLowerCase(),
                token: bearertoken
            };
            var authenticateUrl = "/api/internal/token/authenticate";
            $.ajax({
                url: authenticateUrl,
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(data)
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };

        var getOwnAccount = function(accounts){
            var ownAccount = {accountReference:{id:false}};
            if (accounts.length > 0) {
                accounts.forEach(function(account) {
                    if (account.roles.indexOf('account/owner') >= 0){
                        ownAccount = account;
                    }
                });
            }
            return ownAccount;
        };

        var getUserMemberships = function(){
            var url = "/api/account";
            return $.ajax({
                url: url,
                type: "GET",
            });
        };

        var ensureExistenceAccount = function(accountName) {
            var deferredResult = $.Deferred();
            var url = "/api/account";
            $.ajax({
                url: url,
                type: "GET",
                success: function(membership) {
                    var account = getOwnAccount(membership);
                    if (account.accountReference.id){
                        deferredResult.resolve(account.accountReference); //returns Account
                    }
                    else {
                        // user hasn't memberships
                        createAccount(accountName)
                        .fail(function(error){deferredResult.reject(error);})
                        .then(function(account){
                        deferredResult.resolve(account);
                        });

                    }
                },
                error: function(error){
                    deferredResult.reject(error);
                }
            });
            return deferredResult;
        };

        var createAccount = function(account) {
            var deferredResult = $.Deferred();
            var url = "/api/account";
            var data = {
                name: account
            };
            $.ajax({
                url: url,
                type: "POST",
                data: JSON.stringify(data),
                contentType: "application/json"
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };

        var createWorkspace = function(workspace, account) {
            var deferredResult = $.Deferred();
            var url = "/api/workspace";
            var data = {
                name: workspace,
                accountId: account
            };
            $.ajax({
                url: url,
                type: "POST",
                data: JSON.stringify(data),
                contentType: "application/json"
            })
            .success(function(ws){
                deferredResult.resolve(ws);//return ws
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };

        var login = function(email, password) {
            if (isWebsocketEnabled()) {
                var loginUrl = "/api/auth/login?" + window.location.search.substring(1);
                var data = {
                    username: email,
                    password: password
                };
                return $.ajax({
                    url: loginUrl,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                });
            }
        };

        var getUserInfo = function() {
            var deferredResult = $.Deferred();
            var url = "/api/user";
            $.ajax({
                url: url,
                type: "GET"
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };

        var addMemberToWorkspace = function(workspaceId, userId) {
            var deferredResult = $.Deferred();
            var url = "/api/workspace/" + workspaceId + "/members";
            var data = {
                userId: userId,
                roles: []
            };
            $.ajax({
                url: url,
                type: "POST",
                data: JSON.stringify(data),
                contentType: "application/json"
            })
            .success(function(response){
                deferredResult.resolve(response);
            })
            .error(function(error){
                deferredResult.reject(error);
            });
            return deferredResult;
        };
        var redirectToUrl = function(url) {
            window.location = url;
        };
        
        var appendQuery = function(url) {
            return url  +  window.location.search;
        };
        return {
            removeCookie: removeCookie,
            isWebsocketEnabled: isWebsocketEnabled,
            getQueryParameterByName: getQueryParameterByName,
            appendQuery: appendQuery,
            AccountError: AccountError,
            authenticate: authenticate,
            ensureExistenceAccount: ensureExistenceAccount,
            getOwnAccount: getOwnAccount,
            isValidDomain: function(domain) {
                return (/^[a-z0-9][a-z0-9_.-]{2,19}$/).exec(domain) !== null;
            },
            isValidEmail: function(email) {
                return (/^[^\+\/]+$/).test(email);
            },
            //Password must contain at least one letter, at least one number, and be longer than 8 charaters.
            isValidPassword: function(value) {
                return (/^(?=.*[0-9]+.*)(?=.*[a-zA-Z]+.*)[0-9a-zA-Z]{8,}$/).test(value);
            },

            // redirect to login page if user has 'logged_in' cookie
            redirectIfUserHasLoginCookie: function() {
                if ($.cookie('logged_in')) {
                    window.location = '/site/login' + window.location.search;
                }
            },

            processLogin: function(email, password, redirect_url, success, error){
                var selectWsUrl = "/site/private/select-tenant?cookiePresent&" + window.location.search.substring(1);
                var workspaceId;
                //TODO login refactoring
                login(email, password)
                .then(function() {
                        getUserMemberships()// getUserMemberships()
                        .then(function(accounts){
                            var account = getOwnAccount(accounts);
                            if(!account.accountReference.id){//if user has no account
                                var accountName = email.indexOf('@')>=0?email.substring(0, email.indexOf('@')):email; //email.substring(0, email.indexOf('@'));
                                return createAccount(accountName)//create account
                                .then(function(newAccount){
                                    account = newAccount;
                                    return createWorkspace(accountName, account.id);//create WS
                                })
                                .then(function(workspace){
                                    workspaceId = workspace.id;
                                    return getUserInfo();
                                })
                                .then(function(user){
                                    return addMemberToWorkspace(workspaceId,user.id);//add User to WS
                                });

                            }
                        })
                        .then(function(){
                            if (redirect_url) {
                                success({
                                    url: redirect_url + window.location.hash
                                });
                            } else {
                                success({
                                    url: selectWsUrl
                                });
                            }
                        });

                    })
                    .fail(function(response /*, status , err*/ ) {
                        var responseErr;
                        try{
                            responseErr = JSON.parse(response.responseText).message;
                        }catch(e){
                            console.log(e);
                            responseErr = "Something went wrong. Please try again or contact support";
                        }
                        error([
                            new AccountError(null, responseErr)
                        ]);
                        }
                    );

            },

            onpremLogin: function(username, password, redirect_url, success, error) {
                if (isWebsocketEnabled()) {
                    var loginUrl = "/api/auth/login?" + window.location.search.substring(1);
                    var selectWsUrl = "/site/private/select-tenant?cookiePresent&" + window.location.search.substring(1);
                    var data = {
                        username: username,
                        password: password
                    };
                    $.ajax({
                        url: loginUrl,
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(data),
                        success: function() {
                            if (redirect_url) {
                                success({
                                    url: redirect_url
                                });
                            } else {
                                success({
                                    url: selectWsUrl
                                });
                            }
                        },
                        error: function(response /*, status , err*/ ) {
                            var responseErr;
                            try{
                                responseErr = JSON.parse(response.responseText).message;
                            }catch(e){
                                responseErr = "Authentication: Something went wrong. Please try again or contact support";
                            }


                            error([
                                new AccountError(null, responseErr)
                            ]);
                        }
                    });
                }
            },

            adminLogin: function(email, password, redirect_url, success, error) {
                if (isWebsocketEnabled()) {
                    var loginUrl = "/api/auth/login?" + window.location.search.substring(1);
                    var selectWsUrl = "/site/private/select-tenant?cookiePresent&" + window.location.search.substring(1);
                    var data = {
                        username: email,
                        password: password,
                        realm: "sysldap"
                    };
                    $.ajax({
                        url: loginUrl,
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(data),
                        success: function() {
                            if (redirect_url) {
                                success({
                                    url: redirect_url
                                });
                            } else {
                                success({
                                    url: selectWsUrl
                                });
                            }
                        },
                        error: function(response /*, status , err*/ ) {
                            var responseErr;
                            try{
                                responseErr = JSON.parse(response.responseText).message;
                            }catch(e){
                                responseErr = "Authentication: Something went wrong. Please try again or contact support";
                            }


                            error([
                                new AccountError(null, responseErr)
                            ]);
                        }
                    });
                }
            },
            loginWithGoogle: loginWithGoogle,
            loginWithGithub: loginWithGithub,
            createTenant: function(email, domain, success, error) {
                var data = {
                    email: email.toLowerCase(),
                    workspacename: domain.toLowerCase()
                };
                var emailValidateUrl = "/api/internal/token/validate?" + window.location.search.substring(1);
                $.ajax({
                    url: emailValidateUrl,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success: function() {
                        success({
                            url: '../site/thank-you'
                        });
                    },
                    error: function(xhr /*, status , err*/ ) {
                        error([
                            new AccountError(null, xhr.responseText)
                        ]);
                    }
                });
            },

            processCreate: function(username, bearertoken, workspace, redirect_url, error) {
                var workspaceID;
                authenticate(username, bearertoken)
                .fail(function(response) {
                    var responseErr;
                    try{
                        responseErr = JSON.parse(response.responseText).message;
                    }catch(e){
                        responseErr = "Something went wrong. Please try again or contact support";
                    }


                    error([
                        new AccountError(null, responseErr)
                    ]);
                })
                .then(function(){
                    if (workspace) {
                        ensureExistenceAccount(workspace) // get/create account
                        .then(function(account){
                            return createWorkspace(workspace, account.id);
                        })
                        .then(function(workspace){
                            workspaceID = workspace.id; // store workspace id
                            return getUserInfo();
                        })
                        .then(function(user){
                            return addMemberToWorkspace(workspaceID,user.id);
                        })
                        .done(function() {
                            if (redirect_url) {
                                redirectToUrl(redirect_url);
                            }else {
                                redirectToUrl("/dashboard/");
                            }
                        })
                        .fail(function(response) {
                            var responseErr;
                            try{
                                responseErr = JSON.parse(response.responseText).message;
                            }catch(e){
                                responseErr = "Something went wrong. Please try again or contact support";
                            }


                            error([
                                new AccountError(null, responseErr)
                            ]);
                        });
                    } else if (redirect_url) {
                        redirectToUrl(redirect_url);
                    } else {
                        redirectToUrl("/dashboard/");
                    }

                });

            },
            //--------------------------------- Recover password module
            // Send recover password request
            recoverPassword: function(email, success, error) {
                var passwordRecoveryUrl = "/api/password/recover/" + email;
                $.ajax({
                    url: passwordRecoveryUrl,
                    type: "POST",
                    data: {},
                    success: function(output, status, xhr) {
                        success({
                            message: xhr.responseText
                        });
                    },
                    error: function(response) {
                        var responseErr;
                        try{
                            responseErr = JSON.parse(response.responseText).message;
                        }catch(e){
                            responseErr = "Recover passowrd: Something went wrong. Please try again or contact support";
                        }

                        error([
                            new AccountError(null, responseErr)
                        ]);
                    }
                });
            },
            // Get user email by reset password id
            confirmSetupPassword: function(success, error) {
                var confirmSetupPasswordUrl = "/api/password/verify",
                    id = getQueryParameterByName("id");
                if (typeof id === 'undefined') {
                    error([
                        new AccountError(null, "Invalid password reset url")
                    ]);
                    return;
                }
                $.ajax({
                    url: confirmSetupPasswordUrl + "/" + id,
                    type: "GET",
                    success: function(output, status, xhr) {
                        success({
                            email: xhr.responseText
                        });
                    },
                    error: function(xhr /*,status , err*/ ) {
                        setTimeout(function() {
                            window.location = "/site/recover-password";
                        }, 10000);
                        error([
                            new AccountError(null, xhr.responseText + ".<br>You will be redirected in 10 sec")
                        ]);
                    }
                });
            },
            // Setup new password
            setupPassword: function(password, success, error) {
                // implementation based on this:
                // https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/setup-password.js
                // We assume that uid is part of the url :
                //  https://codenvy.com/pages/setup-password?id=df3c62fe-1459-48af-a4a0-d0c1cc17614a
                var setupPasswordUrl = "/api/password/setup",
                    id = getQueryParameterByName("id");
                $.ajax({
                    url: setupPasswordUrl,
                    type: "POST",
                    data: {
                        uuid: id,
                        password: password
                    },
                    success: function() {
                        success({
                            url: "/site/login"
                        });
                    },
                    error: function(response) {
                        var responseErr;
                        try{
                            responseErr = JSON.parse(response.responseText).message;
                        }catch(e){
                            responseErr = "Setup passowrd: Something went wrong. Please try again or contact support";
                        }

                        error([
                            new AccountError(null, responseErr)
                        ]);
                    }
                });
            },
            //--------------------------------- The end of Recover password module
            /**
             * Encode all special characters including ~!*()'. Replace " " on "+"
             * @see http://xkr.us/articles/javascript/encode-compare/
             * @param {Object} string
             */
            encodeSpecialSymbolsForPost: function(string) {
                if (string) {
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
            escapeSpecialSymbols: function(string) {
                if (string) {
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

            // Changing login page behavior if authtype=ldap
            isAuthtypeLdap: function() {
                var type = getQueryParameterByName("authtype");
                return type;
            }
        };
    });
}(window));
