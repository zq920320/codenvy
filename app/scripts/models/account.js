(function(window){
    define(["jquery"],function($){

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


        /*

            Password Setup Id Provider is used to provide ids
            for confirmSetupPassword and setupPassword functions

        */

        var PasswordSetupIdProvider = {
            getId : function(){
                //get query string parameter by name
                return (function(name)
                {
                    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
                    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
                    var results = regex.exec(window.location.search);
                    if(results){
                        return decodeURIComponent(results[1].replace(/\+/g, " "));
                    }
                })("id");
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


        return {

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
                    /^[a-z]{1}[a-z0-9\-]{0,19}\.codenvy\.com/g
                ).exec(domain) !== null ;
            },

            login : function(email,password,success,error){
                var loginUrl = "/sso/server/gen?authType=jaas",
                    queryString = window.location.search;

                if(queryString != null && queryString.length > 4){ ///?key=value >4 symbols
                    loginUrl += "&" + queryString.substring(1);
                }

                success({ loginUrl: loginUrl })
            },

            createTenant : function(email,domain,success,error){

                var tenantServiceUrl = "/cloud-admin/rest/cloud-admin/public-tenant-service/create-with-confirm";

                var request = $.ajax({
                    url : tenantServiceUrl,
                    type : "POST",
                    data: { domain: encodeURIComponent(domain), email: email },
                    success : function(output, status, xhr){
                        success({url: 'thank-you.jsp'});
                    },
                    error : function(xhr, status, err){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            },

            recoverPassword : function(email,success,error){
                //implementation based on this:
                //https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/recover-password.js

                var passwordRecoveryUrl = "/rest/password/recover/";

                var request = $.ajax({
                    url : passwordRecoveryUrl,
                    type : "POST",
                    data: {email : email},
                    success : function(output, status, xhr){
                        success({message: xhr.responseText});
                    },
                    error : function(xhr, status, err){
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
                // https://codenvy.com/setup-password.jsp?id=df3c62fe-1459-48af-a4a0-d0c1cc17614a

                var confirmSetupPasswordUrl = "/rest/password/setup-confirmed",
                    id = PasswordSetupIdProvider.getId();

                if(typeof id === 'undefined'){
                    error([
                        new AccountError(null,"Invalid password reset url")
                    ]);

                    return;
                }

                var request = $.ajax({
                    url : confirmSetupPasswordUrl + "/" + id,
                    type : "GET",
                    success : function(output, status, xhr){
                        success({ email : xhr.responseText });
                    },
                    error : function(xhr, status, err){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });

            },

            setupPassword : function(password,success,error){
                // implementation based on this:
                // https://github.com/codenvy/cloud-ide/blob/master/cloud-ide-war/src/main/webapp/js/setup-password.js
                // We assume that uid is part of the url :
                //  https://codenvy.com/setup-password.jsp?id=df3c62fe-1459-48af-a4a0-d0c1cc17614a

                var setupPasswordUrl = "/rest/password/setup",
                    id = PasswordSetupIdProvider.getId();


                var request = $.ajax({
                    url : setupPasswordUrl,
                    type : "POST",
                    data : { uuid : id, password : password },
                    success : function(output, status, xhr){
                        success({url: "/"});
                    },
                    error : function(xhr, status, err){
                        error([
                            new AccountError(null,xhr.responseText)
                        ]);
                    }
                });
            }

        };
    });
})(window);
