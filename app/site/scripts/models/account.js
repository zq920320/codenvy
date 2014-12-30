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
    define(["jquery", "json", "models/tenant", "models/useraccounts", "models/profile", "models/workspaces", "cookies"], function($, JSON, Tenant, Accounts, Profile, Workspaces) {
        /*
            AccountError is used to report errors through error callback function
            (see details below ). Example usage:

            new AccountError("password","Your password is too short")

        */
        var userProfile = userProfile || {}; // user Profile to store user's data from server
        var showSupportLink = function(isPaid) {
            var freeLink = $('.footer-link')[2];
            var paidLink = $('#uvTabLabel')[0];
            if (!paidLink & !freeLink) {
                if (isPaid) {
                    var uv = document.createElement('script');
                    uv.type = 'text/javascript';
                    uv.async = true;
                    uv.src = ('https:' === document.location.protocol ? 'https://' : 'http://') + 'widget.uservoice.com/wfZmoiHoOptcKkBgu238zw.js';
                    var s = document.getElementsByTagName('script')[0];
                    s.parentNode.insertBefore(uv, s);
                } else {
                    var el = $("footer").find("ul");
                    el.append('<li><a class="footer-link" href="http://helpdesk.codenvy.com">Feedback & support</a></li>');
                }
            }
        };
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

        var redirectToNonTempWS = function() {
            $.when(Tenant.getTenants()).done(function(tenants) {
                var url;
                switch (tenants.length) {
                    case 0:
                        url = "/site/create-account" + window.location.search;
                        window.location = url;
                        break;
                    default:
                        url = "/ws/" + tenants[0].toJSON().name + window.location.search;
                        window.location = url;

                }
            }).fail(function(msg) {
                window.alert(msg);
            });
        };

        var isBadGateway = function(jqXHR) {
            return jqXHR.status === 502;
        };
        var getQueryParameterByName = function(name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
            var results = regex.exec(window.location.search);
            if (results) {
                return decodeURIComponent(results[1].replace(/\+/g, " "));
            }
        };
        /*START paid support tab*/
        /*global ActiveXObject: false */
        // Verify subscriptions for Organization
        function checkSubscriptionFor(orgId) {
            var request;
            var plansArray = ["Saas", "Factory"];
            var url = "/api/account/" + orgId + "/subscriptions";
            if (window.XMLHttpRequest) { // code for IE7+, Firefox, Chrome, Opera, Safari
                request = new XMLHttpRequest();
            } else { // code for IE6, IE5
                request = new ActiveXObject("Microsoft.XMLHTTP");
            }
            request.onreadystatechange = function() {
                var response;
                var paid = false;
                if (request.readyState === 4 && request.status === 200) {
                    try {
                        response = JSON.parse(request.responseText);
                        if (response.length) {
                            if (response.some(function(sub) {
                                return (plansArray.indexOf(sub.serviceId) >= 0);
                            })) {
                                paid = true;
                            }
                        }
                        showSupportLink(paid);
                    } catch (err) {
                        showSupportLink(false);
                    }
                }
            };
            request.open("GET", url, true);
            request.send();
        }
        // Sets Info for Premium User 
        function setPremiumUserInfo() {
            var request;
            var url = "/api/account";
            if (window.XMLHttpRequest) { // code for IE7+, Firefox, Chrome, Opera, Safari
                request = new XMLHttpRequest();
            } else { // code for IE6, IE5
                request = new ActiveXObject("Microsoft.XMLHTTP");
            }
            request.onreadystatechange = function() {
                var response;
                /*var accountOwnerIndex=null;*/
                if (request.readyState === 4 && request.status === 200) {
                    response = JSON.parse(request.responseText);
                    if (response.length > 0) {
                        response.forEach(function(account, index) {
                            var isOwner = (account.roles.indexOf('account/owner') >= 0);
                            if (isOwner) {
                                checkSubscriptionFor(response[index].accountReference.id);
                            }
                        });
                    } else {
                        showSupportLink(false);
                    }
                }
            };
            request.open("GET", url, true);
            request.send();
        }
        /*END paid support tab*/
        /*

            Filling the Profile page
        */
        function onReceiveUserProfileInfo(response) {
            userProfile = response.attributes;
            var profileAttributes = userProfile.attributes;
            var attributes = {};
            for (var key in profileAttributes) {
                // Get attributes only for Profile page
                var profilePageAttributes = ["firstName", "lastName", "phone", "employer", "jobtitle", "email"];
                if (profilePageAttributes.indexOf(key) >= 0) {
                    Object.defineProperty(attributes, key, {
                        value: profileAttributes[key]
                    });
                }
            }
            document.getElementById("account_value").innerHTML = attributes.email || "";
            document.getElementsByName("first_name")[0].value = attributes.firstName || "";
            document.getElementsByName("last_name")[0].value = attributes.lastName || "";
            document.getElementsByName("phone_work")[0].value = attributes.phone || "";
            document.getElementsByName("company")[0].value = attributes.employer || "";
            document.getElementsByName("title")[0].value = attributes.jobtitle || "";
        }
        var loginWithGoogle = function(page, callback) {
            if (isWebsocketEnabled()) {
                _gaq.push(['_trackEvent', 'Regisration', 'Google registration', page]);
                var url = "/api/oauth/authenticate?oauth_provider=google&mode=federated_login" + "&scope=https://www.googleapis.com/auth/userinfo.profile&scope=https://www.googleapis.com/auth/userinfo.email" + "&redirect_after_login=" + encodeURIComponent(window.location.protocol + "//" + window.location.host + "/api/oauth?" + window.location.search.substring(1) + "&oauth_provider=google");
                if (typeof callback !== 'undefined') {
                    callback(url);
                }
            }
        };
        var loginWithGithub = function(page, callback) {
            if (isWebsocketEnabled()) {
                _gaq.push(['_trackEvent', 'Regisration', 'GitHub registration', page]);
                var url = "/api/oauth/authenticate?oauth_provider=github&mode=federated_login&scope=user&scope=repo" + "&redirect_after_login=" + encodeURIComponent(window.location.protocol + "//" + window.location.host + "/api/oauth?" + window.location.search.substring(1) + "&oauth_provider=github");
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
                success: function(xhr, status, membership) {
                    if (getOwnAccount(membership).accountReference.id){
                        deferredResult.resolve(membership.accountReference); //returns Account
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
        return {
            removeCookie: removeCookie,
            isWebsocketEnabled: isWebsocketEnabled,
            getQueryParameterByName: getQueryParameterByName,
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
            // redirect to login page if user has 'logged_in' cookie
            redirectIfUserHasLoginCookie: function() {
                if ($.cookie('logged_in')) {
                    window.location = '/site/login' + window.location.search;
                }
            },

            processLogin: function(email, password, redirect_url, success, error){
                var selectWsUrl = "../site/private/select-tenant?cookiePresent&" + window.location.search.substring(1);
                //TODO login refactoring
                login(email, password)
                .then(function() {
                        getUserMemberships()// getUserMemberships()
                        .then(function(accounts){
                            var account = getOwnAccount(accounts);
                            if(!account.accountReference.id){//if user has no account
                                var userId = accounts[0].userId;
                                var accountName = email.substring(0, email.indexOf('@'));
                                return createAccount(accountName)//create account
                                .then(function(newAccount){
                                    account = newAccount;
                                    return createWorkspace(accountName, account.id);//create WS
                                })
                                .then(function(workspace){
                                    return addMemberToWorkspace(workspace.id,userId);//add User to WS
                                });
                                
                            }
                        })
                        .then(function(){
                            if (redirect_url) {
                                success({
                                    url: redirect_url
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
            adminLogin: function(email, password, redirect_url, success, error) {
                if (isWebsocketEnabled()) {
                    var loginUrl = "/api/auth/login?" + window.location.search.substring(1);
                    var selectWsUrl = "../site/private/select-tenant?cookiePresent&" + window.location.search.substring(1);
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

            processCreate: function(username, bearertoken, src_workspace, workspace, redirect_url, queryParams, error) {
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
                            } else if (src_workspace) {
                                // Initializer.processWaitForTenant(workspace, "", "?" + queryParams);
                            } else {
                                //Initializer.processWaitForTenant(workspace, "create", "?" + queryParams);
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
                    } else if (src_workspace) {
                        redirectToNonTempWS();
                    } else if (redirect_url) {
                        redirectToUrl(redirect_url);
                    } else {
                        redirectToUrl("/dashboard/");
                    }

                });
              
            },
            
            joinWorkspace: function(username, bearertoken, workspace, success, error) {
                var data = {
                    username: username.toLowerCase(),
                    token: bearertoken
                };
                var destinationUrl = window.location.protocol + "//" + window.location.host + "/ws/" + workspace;
                var waitUrl = "../wait-for-tenant?type=start&tenantName=" + workspace + "&redirect_url=" + encodeURIComponent(destinationUrl);
                //var workspaceName = {name: workspace};
                var authenticateUrl = "/api/internal/token/authenticate";
                $.ajax({
                    url: authenticateUrl,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success: function() {
                        success({
                            url: waitUrl
                        });
                    },
                    error: function(xhr /*, status , err*/ ) {
                        error([
                            new AccountError(null, xhr.responseText)
                        ]);
                    }
                });
            },
            recoverPassword: function(email, success, error) {
                //implementation based on this:
                //https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/recover-password.js
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
                    error: function(xhr) {
                        error([
                            new AccountError(null, JSON.parse(xhr.responseText).message)
                        ]);
                    }
                });
            },
            confirmSetupPassword: function(success, error) {
                // implementation based on this:
                // https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/setup-password.js
                // just like with setupPassword, we expect the id to be in the url:
                // https://codenvy.com/pages/setup-password?id=df3c62fe-1459-48af-a4a0-d0c1cc17614a
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
                    error: function(xhr) {
                        error([
                            new AccountError(null, xhr.responseText)
                        ]);
                    }
                });
            },
            // change password in Profile page
            changePassword: function(password, success, error) {
                var changePasswordUrl = "/api/user/password";
                $.ajax({
                    url: changePasswordUrl,
                    type: "POST",
                    data: "password=" + password,
                    success: function() {
                        success({
                            url: "/"
                        });
                    },
                    error: function(xhr) {
                        error([
                            new AccountError(null, xhr.responseText)
                        ]);
                    }
                });
            },
            // update User`s profile in Profile page
            updateProfile: function(userAttributes, success, error) {
                // userProfile.attributes = body;//Updating profile attributes
                Object.getOwnPropertyNames(userAttributes).forEach(function(prop) {
                    userProfile.attributes[prop] = userAttributes[prop];
                });
                var data = JSON.stringify(userProfile.attributes);
                $.ajax({
                    url: "/api/profile",
                    type: "POST",
                    data: data,
                    contentType: "application/json; charset=utf-8",
                    success: function() {
                        success();
                    },
                    error: function(xhr) {
                        error([
                            new AccountError(null, xhr.responseText)
                        ]);
                    }
                });
            },
            // get User`s profile in Profile page
            getUserProfile: function(success, error) {
                $.when(Profile.getUser()).done(function(user) {
                    onReceiveUserProfileInfo(user);
                }).fail(function(msg) {
                    error([
                        new AccountError(null, msg)
                    ]);
                });
            },
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
            getTenants: function(success, error, redirect) {
                $.when(Tenant.getTenants()).done(function(tenants) {
                    switch (tenants.length) {
                        case 0:
                            redirect({
                                url: "/site/create-account"
                            });
                            break;
                        case 1:
                            redirect({
                                url: "/ws/" + tenants[0].toJSON().name
                            });
                            break;
                        default:
                            $.when(Profile.getUser()).done(function(user) {
                                success(tenants, user);
                            }).fail(function(msg) {
                                error([
                                    new AccountError(null, msg)
                                ]);
                            });
                    }
                }).fail(function(msg) {
                    error([
                        new AccountError(null, msg)
                    ]);
                });
            },
            // Braintree payment: select account for payment 
            getAccounts: function(payment, success, error, redirect) {
                $.when(Accounts.getAccounts()).done(function(accounts) {
                    switch (accounts.length) {
                        case 0:
                            redirect({
                                url: "/site/error/no-valid-workspaces"
                            });
                            break;
                        default:
                            $.each(accounts, function(index, account) {
                                $.when(Workspaces.getWorkspaces(account.id)).done(function(workspaces) {
                                    switch (workspaces.length) {
                                        case 0:
                                            redirect({
                                                url: "/site/error/no-valid-workspaces"
                                            });
                                            break;
                                            /*case 1: success(workspaces);
                                            break;*/
                                        default:
                                            success(workspaces, payment);
                                    }
                                }).fail(function(msg) {
                                    error([
                                        new AccountError(null, msg)
                                    ]);
                                });
                            });
                    }
                }).fail(function(msg) {
                    error([
                        new AccountError(null, msg)
                    ]);
                });
            },
            // Returns true if User has WS with tariff plan
            supportTab: function() {
                if ($.cookie("logged_in")) {
                    setPremiumUserInfo();
                } else {
                    showSupportLink(false);
                }
            },
            addSubscription: function(form, workspaceId, showPaymentForm, success, error) {
                // Get accountId for current User
                var url = "/api/account";
                $.ajax({
                    url: url,
                    type: "GET",
                    success: function(data) {
                        //Get accountId
                        sendSubscriptionRequest(data[0].id, workspaceId, form, showPaymentForm, success, error);
                    },
                    error: function(response) {
                        //Show error
                        error([
                            new AccountError(null, "Authentication Error" + response.message)
                        ]);
                    }
                });
                /*form = $('#codenvy-add-subscription-form');
                e.preventDefault();*/
                var sendSubscriptionRequest = function(accountId, workspaceId, form, showPaymentForm, success, error) {
                    var addSubscriptionUrl = "/api/account/subscriptions/";
                    var data = {};
                    var serviceId = getQueryParameterByName("serviceId"),
                        startDate = getQueryParameterByName("startDate"),
                        endDate = getQueryParameterByName("endDate"),
                        Package = getQueryParameterByName("Package"),
                        RAM = getQueryParameterByName("RAM"),
                        TariffPlan = getQueryParameterByName("TariffPlan");
                    if (serviceId) { //If serviceId not exists in query params - throw error
                        data.serviceId = serviceId;
                        data.accountId = accountId;
                        if (startDate) {
                            data.startDate = startDate;
                        }
                        if (endDate) {
                            data.endDate = endDate;
                        }
                        data.properties = {
                            "codenvy:workspace_id": workspaceId
                        };
                        if (Package) {
                            data.properties.Package = Package;
                        }
                        if (RAM) {
                            data.properties.RAM = RAM;
                        }
                        if (TariffPlan) {
                            data.properties.TariffPlan = TariffPlan;
                        }
                        $.ajax({
                            url: addSubscriptionUrl,
                            type: "POST",
                            contentType: "application/json",
                            data: JSON.stringify(data),
                            success: function() {
                                success('Subscription added succesfully');
                            },
                            error: function(response) {
                                if (response.status === 402) {
                                    var subscription = JSON.parse(response.responseText);
                                    showPaymentForm(subscription.id);
                                } else {
                                    error([
                                        new AccountError(null, response.responseText)
                                    ]);
                                }
                            }
                        });
                    } else {
                        error([
                            new AccountError(null, "Not found serviceId parameter. Error")
                        ]);
                    }
                };
            },
            paymentFormSubmit: function(subscriptionid, success, error) {
                //var subscriptionid = $("input[name=subscriptionid]")[0].value;
                var purchaseUrl = "/api/account/subscriptions/" + subscriptionid + "/purchase";
                var data = {
                    cardholderName: $('input[name=cardholderName]')[0].value,
                    //subscriptionid:$('input[name=subscriptionid]')[0].value,
                    subscriptionid: subscriptionid,
                    cardNumber: $('input[name=cardNumber]')[0].value,
                    cvv: $('input[name=cvv]')[0].value,
                    expirationMonth: $('input[name=expirationMonth]')[0].value,
                    expirationYear: $('input[name=expirationYear]')[0].value
                };
                $.ajax({
                    url: purchaseUrl,
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    success: function() {
                        success("Subscription added succesfully.");
                    },
                    error: function(response) {
                        var paymentError, message;
                        paymentError = JSON.parse(response.responseText);
                        if (paymentError.message) {
                            message = paymentError.message;
                        } else {
                            message = "Payment error cccurred. Please contact support.";
                        }
                        error([
                            new AccountError(null, message)
                        ]);
                    }
                });
            },
            // Changing login page behavior if authtype=ldap
            isAuthtypeLdap: function() {
                var type = getQueryParameterByName("authtype");
                return type;
            },
            waitForTenant: function(success, error) {
                //based on : https://github.com/codenvy/cloud-ide/blob/8fe1e50cc6434899dfdfd7b2e85c82008a39a880/cloud-ide-war/src/main/webapp/js/wait-tenant-creation.js
                var type = getQueryParameterByName("type"); //create OR start
                var redirectUrl = getQueryParameterByName("redirect_url");
                var tenantName = getQueryParameterByName("tenantName");
                if (typeof tenantName === 'undefined') {
                    error([
                        new AccountError(null, "This is not a valid url")
                    ]);
                }
                var MAX_WAIT_TIME_SECONDS = 180,
                    PING_TIMEOUT_MILLISECONDS = 500,
                    endTime = new Date().getTime() + MAX_WAIT_TIME_SECONDS * 1000;

                function buildRedirectUrl() {
                    return redirectUrl;
                }

                function hitServer() {
                    if (new Date().getTime() >= endTime) {
                        // removing autologin cookie if exist
                        removeCookie("autologin");
                        if (type === "create") {
                            error([
                                new AccountError(null, "Workspace creation delayed. We'll email you the credentials after your workspace is created.")
                            ]);
                        } else if (type === "factory") {
                            window.location = "/site/error/error-factory-creation";
                        } else {
                            error([
                                new AccountError(null, "The requested workspace <strong>'" + tenantName + "'</strong> is not available. Please, contact support.")
                            ]);
                        }
                        return;
                    }
                    $.ajax({
                        url: "/cloud-admin/rest/cloud-admin/tenant-service/tenant-state/" + tenantName,
                        type: "GET",
                        success: function(output, status, xhr) {
                            if (xhr.responseText === "ONLINE") {
                                success({
                                    url: buildRedirectUrl()
                                });
                            } else if (xhr.responseText === "CREATION_FAIL") {
                                success({
                                    url: "/site/error/error-create-tenant"
                                });
                            } else {
                                setTimeout(hitServer, PING_TIMEOUT_MILLISECONDS);
                            }
                        },
                        error: function(xhr) {
                            if (isBadGateway(xhr)) {
                                error([
                                    new AccountError(null, "The requested workspace is not available. Please, contact support.")
                                ]);
                            } else {
                                error([
                                    new AccountError(null, xhr.responseText)
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