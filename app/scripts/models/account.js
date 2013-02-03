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
                /^[a-z]{1}[a-z0-9-]{0,19}\.codenvy\.com/g
            ).exec(domain) !== null ;
        },

        signUp : function(email,domain,success,error){

            console.log("signing you up", email, domain);

            //example

            //hit the server with the data we got back


        },

        login : function(email,password,domain,success,error){

            var loginUrl =  "/sso/server/gen"
            + "?username=" + email
            + "&password=" + password
            + "&redirectTenantName=" + domain
            + "&authType=jaas";

            var request = $.ajax({
                url : loginUrl,
                type: POST,
                data: {
                    username:email,
                    password:password,
                    redirectTenantName:tenant,
                    //encodedQueryParam: encodeURIComponent(queryString);
                },
                success : function(output, status, xhr){
                        success();
                },
                error : function(xhr, status, err){
                    error(err);
                }
            });
        },

        createTenant : function(email,domain,success,error){

            var tenantServiceUrl = "/cloud-admin/rest/cloud-admin/public-tenant-service/create-with-confirm";
            var createTenantUrl = tenantServiceUrl + "/" + encodeURIComponent(domain) + "/" + email;

            var request = $.ajax({
                url : createTenantUrl,
                type: POST,
                success : function(output, status, xhr){

                        //Succes, redirect to thankyou.jsp
                        success({url: 'thankyou.jsp'});
                },
                error : function(xhr, status, err){

                    //else error code, display error message
                    error(err);
                }
            });
        },

        recoverPassword : function(email,success,error){

        }

    };
});
